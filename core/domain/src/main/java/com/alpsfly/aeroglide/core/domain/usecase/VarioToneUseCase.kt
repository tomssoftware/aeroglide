package com.alpsfly.aeroglide.core.domain.usecase

import android.content.SharedPreferences
import com.alpsfly.aeroglide.core.common.audio.VarioTone
import com.alpsfly.aeroglide.core.common.di.ApplicationScope
import com.alpsfly.aeroglide.core.data.SensorRepository
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

// SharedPreferences keys – must match the values written by SettingsViewModel.
private const val PREFS_KEY_CLIMB_THRESHOLD = "spk_vario_tone_threshold_climb"
private const val PREFS_KEY_SINK_THRESHOLD = "spk_vario_tone_threshold_sink"

// Default thresholds used when the user has not changed the settings yet.
private const val DEFAULT_CLIMB_THRESHOLD = 0.2f  // m/s
private const val DEFAULT_SINK_THRESHOLD = -0.3f  // m/s (negative)

/**
 * Manages the variometer tone lifecycle.
 *
 * Observes [SensorRepository.climbrateFlowUi], maps each 1-second averaged climb-rate
 * sample to a [VarioToneState] using the LX-Navia tone algorithm, and drives [VarioTone]
 * accordingly.  Tone parameters are updated immediately whenever the mapped
 * [VarioToneState] changes (including changes within the same zone).
 *
 * ## Tone algorithm (real-vario style)
 * | Zone   | Frequency formula                                          | Note                             |
 * |--------|------------------------------------------------------------|----------------------------------|
 * | Climb  | `700 × 2^(cr/5)` clipped to [700, 2800] Hz (exp. curve)   | pulsed; asymmetric duty-cycle    |
 * | Sink   | `(500 + cr × 30)` clipped to [220, 500] Hz                 | continuous; cr < 0               |
 * | Silence| –                                                          | dead-zone between thresholds     |
 *
 * Climb beep timing: `totalPeriodMs = (600 − cr × 35) in [120, 600] ms`,
 * `dutyCycle = (0.35 + cr × 0.04) in [0.35, 0.70]`,
 * `beepMs = totalPeriodMs × dutyCycle`, `pauseMs = totalPeriodMs − beepMs`.
 *
 * ## Lifecycle
 * The use case is a [Singleton] and outlives Activities.  Call [enable] / [disable]
 * from the UI layer; the [ApplicationScope] coroutine keeps the tone alive during
 * screen rotation.
 *
 * @param sensorRepository Source of [SensorRepository.climbrateFlowUi].
 * @param varioTone        Audio backend that produces the actual tones.
 * @param prefs            App-wide [SharedPreferences] for reading threshold settings.
 * @param scope            Long-lived [CoroutineScope] tied to the application process.
 */
@Singleton
class VarioToneUseCase @Inject constructor(
    private val sensorRepository: SensorRepository,
    private val varioTone: VarioTone,
    private val prefs: SharedPreferences,
    @param:ApplicationScope private val scope: CoroutineScope,
) {

    // --- Public state ---

    private val _isToneEnabled = MutableStateFlow(false)

    /**
     * `true` while the tone engine is active and listening to climb-rate updates.
     * Observed by the UI to reflect the correct volume icon in the top app bar.
     */
    val isToneEnabled: StateFlow<Boolean> = _isToneEnabled.asStateFlow()

    // --- Internal job handle ---

    // Holds the active collection job; null when the tone is disabled.
    private var collectJob: Job? = null
    // Monotonic session id to invalidate stale collector callbacks.
    @Volatile
    private var activeSessionId: Long = 0L

    // --- Public API ---

    /**
     * Activates the tone engine.
     *
     * Starts collecting [SensorRepository.climbrateFlowUi] and drives [varioTone]
     * based on the current climb rate and the user-configured thresholds.
     * Calling [enable] while already enabled is a no-op.
     */
    fun enable() {
        if (_isToneEnabled.value) return
        Timber.i("VarioToneUseCase: enabling tone engine")
        val sessionId = activeSessionId + 1L
        activeSessionId = sessionId
        _isToneEnabled.value = true
        collectJob = scope.launch { collectAndPlay(sessionId) }
    }

    /**
     * Deactivates the tone engine and silences any currently playing tone.
     *
     * Cancels the internal collection job and releases [varioTone] after cancellation
     * has completed. Calling [disable] while already disabled is a no-op.
     */
    fun disable() {
        if (!_isToneEnabled.value) return
        Timber.i("VarioToneUseCase: disabling tone engine")
        _isToneEnabled.value = false
        val disableSessionId = activeSessionId + 1L
        activeSessionId = disableSessionId
        val jobToCancel = collectJob
        collectJob = null

        scope.launch {
            // Wait until collection has fully stopped to avoid post-shutdown tone commands.
            jobToCancel?.cancelAndJoin()
            // If no newer session started in the meantime, release audio resources.
            if (!_isToneEnabled.value && activeSessionId == disableSessionId) {
                varioTone.shutdown()
            }
        }
    }

    // --- Internal implementation ---

    /**
     * Collects [SensorRepository.climbrateFlowUi], maps each sample to a [VarioToneState],
     * and drives [varioTone] whenever the mapped state changes.
     *
     * Thresholds are read once per activation from [SharedPreferences] so a settings change
     * takes effect the next time the user enables the tone.
     */
    private suspend fun collectAndPlay(sessionId: Long) {
        // Read thresholds once at activation time.
        val climbThreshold = prefs.getFloat(PREFS_KEY_CLIMB_THRESHOLD, DEFAULT_CLIMB_THRESHOLD)
        val sinkThreshold = prefs.getFloat(PREFS_KEY_SINK_THRESHOLD, DEFAULT_SINK_THRESHOLD)
        Timber.d("VarioToneUseCase: climbThreshold=$climbThreshold, sinkThreshold=$sinkThreshold")

        var lastState: VarioToneState = VarioToneState.Silence

        sensorRepository.climbrateFlowUi.collect { climbrate ->
            if (!_isToneEnabled.value || activeSessionId != sessionId) return@collect

            val newState = mapToState(climbrate.climbrate, climbThreshold, sinkThreshold)

            // Apply every actual state change so frequency/beep timing follow lift changes
            // immediately, while equal consecutive states are still de-duplicated.
            if (newState != lastState) {
                if (!_isToneEnabled.value || activeSessionId != sessionId) return@collect
                Timber.d("VarioToneUseCase: state update $lastState → $newState @ ${climbrate.climbrate} m/s")
                lastState = newState
                when (newState) {
                    is VarioToneState.Climb -> varioTone.startClimbTone(
                        newState.frequencyHz,
                        newState.beepMs,
                        newState.pauseMs,
                    )

                    is VarioToneState.Sink -> varioTone.startSinkTone(newState.frequencyHz)
                    is VarioToneState.Silence -> varioTone.stop()
                }
            }
        }
    }

    /**
     * Maps a raw climb-rate value to a [VarioToneState] using the LX-Navia algorithm.
     *
     * @param climbrate      Current climb rate in m/s (positive = climbing, negative = sinking).
     * @param climbThreshold Minimum climb rate in m/s above which climb tone is active.
     * @param sinkThreshold  Maximum (negative) sink rate in m/s below which sink tone is active.
     */
    private fun mapToState(
        climbrate: Float,
        climbThreshold: Float,
        sinkThreshold: Float,
    ): VarioToneState = when {
        climbrate > climbThreshold -> {
            // Exponential pitch curve: one octave every ~5 m/s → sounds natural to the ear.
            val frequencyHz = (700f * 2f.pow(climbrate / 5f)).coerceIn(700f, 2800f)

            // Asymmetric duty cycle:
            //   slow climb → short beep, long pause (calm, watchful)
            //   fast climb → long beep, short pause (urgent)
            val totalPeriodMs = (600L - (climbrate * 35f).toLong()).coerceIn(120L, 600L)
            val dutyCycle = (0.35f + climbrate * 0.04f).coerceIn(0.35f, 0.70f)
            val beepMs = (totalPeriodMs * dutyCycle).toLong()
            val pauseMs = totalPeriodMs - beepMs

            VarioToneState.Climb(
                frequencyHz = frequencyHz,
                beepMs = beepMs,
                pauseMs = pauseMs,
            )
        }

        climbrate < sinkThreshold -> VarioToneState.Sink(
            // climbrate is negative here, so frequency decreases as sink worsens.
            frequencyHz = (500f + climbrate * 30f).coerceIn(220f, 500f),
        )

        else -> VarioToneState.Silence
    }

}


