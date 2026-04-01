package com.alpsfly.aeroglide.core.domain.usecase

import androidx.annotation.VisibleForTesting
import com.alpsfly.aeroglide.core.common.di.ApplicationScope
import com.alpsfly.aeroglide.core.data.AutoStartSettingsProvider
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.tinder.StateMachine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Orchestrates automatic take-off and landing detection by connecting
 * [SensorRepository] data and [AutoStartSettingsProvider] configuration to [AutoStartDetector].
 *
 * Acts as a thin coordinator: it owns no application state itself and only
 * forwards detected events through [onTakeOffDetected] and [onLandingDetected].
 * Call [start] to activate sensor collection and [stop] to release all resources.
 *
 * @see AutoStartDetector
 */
// Singleton: the init block wires callbacks on the shared AutoStartDetector.
// Multiple instances would silently overwrite each other's callbacks.
@Singleton
class AutoStartProcessor @Inject constructor(
    private val sensorRepository: SensorRepository,
    private val settingsProvider: AutoStartSettingsProvider,
    @param:ApplicationScope private val applicationScope: CoroutineScope
) {

    // --- Internal State ---

    private val autoStartDetector = AutoStartDetector()

    /**
     * Combines location and climb-rate emissions and feeds each tick into [AutoStartDetector.detect].
     *
     * Launched in [applicationScope] so detection survives Activity recreation.
     * **Must** be cancelled by calling [stop] when auto-start is disabled to release
     * the sensor wake-lock.
     */
    private var collectorJob: Job? = null

    /** Holds subscriptions to individual settings Flows so they can be cancelled together in [stop]. */
    private val settingsJobs = mutableListOf<Job>()

    // --- Callbacks ---

    /** Invoked exactly once when [AutoStartDetector] transitions to [AutoStartDetector.Companion.State.TakeOff]. */
    var onTakeOffDetected: () -> Unit = {}

    /** Invoked exactly once when [AutoStartDetector] transitions to [AutoStartDetector.Companion.State.Landed]. */
    var onLandingDetected: () -> Unit = {}

    init {
        // The processor forwards detector events through simple callbacks so it stays
        // ignorant of application state (recording, UI, etc.).
        autoStartDetector.onTakeOff = {
            Timber.i("AutoStartProcessor: Take-off detected, invoking callback.")
            onTakeOffDetected()
        }
        autoStartDetector.onLanded = {
            Timber.i("AutoStartProcessor: Landing detected, invoking callback.")
            onLandingDetected()
        }
    }

    // --- Public API ---

    /**
     * Starts collecting sensor data and observing settings changes.
     *
     * Idempotent: calling [start] while already running has no effect.
     */
    @OptIn(FlowPreview::class)
    fun start() {
        if (collectorJob?.isActive == true) return
        Timber.i("AutoStartProcessor: Starting.")

        // Clear previous subscriptions if job was aborted without stop
        settingsJobs.forEach { it.cancel() }
        settingsJobs.clear()

        // Mirror every settings change into the detector immediately so thresholds
        // are always in sync without requiring a restart.
        settingsJobs += settingsProvider.velocityLimitTakeOff
            .onEach { autoStartDetector.velocityFlying = it }
            .launchIn(applicationScope)

        settingsJobs += settingsProvider.velocityLimitLanding
            .onEach { autoStartDetector.velocityLanded = it }
            .launchIn(applicationScope)

        settingsJobs += settingsProvider.climbrateLimitTakeOff
            .onEach { autoStartDetector.climbrateTakeOff = it }
            .launchIn(applicationScope)

        settingsJobs += settingsProvider.climbrateLimitLanding
            .onEach { autoStartDetector.climbrateLanding = it }
            .launchIn(applicationScope)

        collectorJob = combine(
            sensorRepository.locationFlowUi,
            sensorRepository.climbrateFlowUi
        ) { location, climbrate ->
            autoStartDetector.velocity = location.speed
            autoStartDetector.climbrate = climbrate.climbrate
            autoStartDetector.detect()
        }.sample(1.seconds).launchIn(applicationScope)
    }

    /**
     * Manually triggers a take-off without waiting for the automatic detection timer.
     *
     * Delegates to [AutoStartDetector.start]. The [onTakeOffDetected] callback fires
     * exactly as it would for an automatically detected take-off, so recording starts
     * normally. Only effective when sensor collection is active (after [start]).
     */
    fun manualStart() {
        Timber.i("AutoStartProcessor: Manual start requested.")
        autoStartDetector.start()
    }

    /**
     * Manually triggers a landing without waiting for the automatic detection timer.
     *
     * Delegates to [AutoStartDetector.stop]. The [onLandingDetected] callback fires
     * exactly as it would for an automatically detected landing, so recording stops
     * normally. Only effective when sensor collection is active (after [start]).
     */
    fun manualStop() {
        Timber.i("AutoStartProcessor: Manual stop requested.")
        autoStartDetector.stop()
    }

    /**
     * Stops sensor collection, cancels all settings subscriptions, and resets the detector.
     *
     * Idempotent: returns early only when **both** [collectorJob] and [settingsJobs] have
     * no active work. Checking only [collectorJob] would leave settings observers running
     * and the detector mutating thresholds if [collectorJob] was cancelled or completed
     * unexpectedly outside of this method.
     */
    fun stop() {
        val hasActiveCollector = collectorJob?.isActive == true
        val hasActiveSettingsJobs = settingsJobs.any { it.isActive }

        if (!hasActiveCollector && !hasActiveSettingsJobs) return

        Timber.i("AutoStartProcessor: Stopping (collector=$hasActiveCollector, settingsJobs=$hasActiveSettingsJobs).")
        collectorJob?.cancel()
        collectorJob = null
        settingsJobs.forEach { it.cancel() }
        settingsJobs.clear()
        autoStartDetector.reset()
    }
}

/**
 * Detects take-off and landing events based on velocity and climb-rate sensor data.
 *
 * Operates as a finite state machine:
 * [Companion.State.WaitForTakeOff] → [Companion.State.TakeOff] → [Companion.State.Flying]
 * → [Companion.State.Landing] → [Companion.State.Landed].
 *
 * Each transition requires the corresponding sensor condition to be satisfied
 * continuously for a minimum [Duration] (e.g. [takeOffDuration]).
 * If a condition breaks before the timer expires the accumulator is reset,
 * preventing false positives from brief sensor spikes.
 *
 * @param timeProvider Returns the current wall-clock time in milliseconds.
 *                     Override with a fake clock in unit tests for deterministic behaviour.
 * @see AutoStartProcessor
 */
class AutoStartDetector(
    private val timeProvider: () -> Long = { System.currentTimeMillis() }
) {

    // --- Sensor Inputs ---

    /** Vertical speed in m/s. Positive = climbing, negative = sinking. */
    var climbrate: Float = Float.MIN_VALUE

    /** Ground speed in m/s as reported by the GPS sensor. */
    var velocity: Float = Float.MIN_VALUE

    // --- Callbacks ---

    /** Invoked exactly once when the machine transitions to [Companion.State.TakeOff]. */
    var onTakeOff: () -> Unit = {}

    /** Invoked exactly once when the machine transitions to [Companion.State.Landed]. */
    var onLanded: () -> Unit = {}

    // --- Detection Thresholds ---

    // Default velocity thresholds are ~10 km/h expressed in m/s.
    // Derivation: 1.38f ≈ 5 km/h / 3.6  →  2 * 1.38f ≈ 2.76 m/s ≈ 10 km/h.

    /** Minimum ground speed in m/s that is considered "airborne" for take-off detection (~10 km/h). */
    var velocityFlying = 2 * 1.38f

    /** Maximum ground speed in m/s that qualifies as "on the ground" for landing confirmation (~10 km/h). */
    var velocityLanded = 2 * 1.38f

    /** Minimum climb rate in m/s required to trigger the take-off state. */
    var climbrateTakeOff = 0.5f

    /** Maximum (most negative) climb rate in m/s that marks the start of a landing approach. */
    var climbrateLanding = -0.5f

    // --- Guard Conditions (private helpers) ---

    // >= is intentional: a value exactly at the threshold still qualifies (inclusive boundary).
    private fun isTakeOffCondition() = velocity >= velocityFlying && climbrate >= climbrateTakeOff
    private fun isAscentCondition() = isTakeOffCondition() // Take-off and ascent share the same condition
    private fun isFlyingCondition() = velocity >= velocityFlying
    private fun isLandingCondition() = velocity >= velocityFlying && climbrate <= climbrateLanding
    private fun isLandedCondition() =  velocity <= velocityLanded && (climbrate in climbrateLanding..climbrateTakeOff)

    // --- Time-Based Guard Durations ---

    /** Minimum continuous time in take-off condition before [Companion.SideEffect.TakeOff] fires. */
    @VisibleForTesting
    internal val takeOffDuration = 5.seconds

    /** Minimum continuous time in flying condition before transitioning to [Companion.State.Flying]. */
    @VisibleForTesting
    internal val flyingDuration = 3.seconds

    /** Minimum continuous time in landing condition before transitioning to [Companion.State.Landing]. */
    @VisibleForTesting
    internal val landingDuration = 3.seconds

    /** Minimum continuous time in landed condition before [Companion.SideEffect.Landed] fires. */
    @VisibleForTesting
    internal val landedDuration = 5.seconds

    /**
     * Idle time after landing before the machine resets to [Companion.State.WaitForTakeOff].
     *
     * Prevents an immediate re-trigger when the pilot taxis after touchdown.
     */
    @VisibleForTesting
    internal val resetDuration = 30.seconds

    // --- Time Accumulators ---

    // Each accumulator tracks how long the corresponding guard condition has been met
    // consecutively. They are zeroed on any state transition or condition break.
    private var timeInTakeOffCondition = Duration.ZERO
    private var timeInFlyingCondition = Duration.ZERO
    private var timeInLandingCondition = Duration.ZERO
    private var timeInLandedCondition = Duration.ZERO

    /**
     * Absolute wall-clock timestamp (from [timeProvider]) recorded the moment the machine
     * enters [Companion.State.Landed]. Reset to 0 on any other state transition.
     *
     * Used by [doReset] to check elapsed time unconditionally, so the reset timer runs
     * independently of what the pilot does on the ground (taxi, walk, etc.).
     * This replaces the old condition-based `timeInResetCondition` accumulator that was
     * erroneously zeroed whenever `isLandedCondition()` broke (e.g. during taxiing).
     */
    private var landedAtTime: Long = 0L

    private fun doTakeOff() = timeInTakeOffCondition >= takeOffDuration
    private fun doFlying() = timeInFlyingCondition >= flyingDuration
    private fun doLanding() = timeInLandingCondition >= landingDuration
    private fun doLanded() = timeInLandedCondition >= landedDuration

    /**
     * Returns true when [resetDuration] has elapsed since the machine entered [Companion.State.Landed].
     *
     * Intentionally does **not** check any sensor condition so taxiing, walking, or any
     * other ground movement cannot block the transition back to [Companion.State.WaitForTakeOff].
     */
    private fun doReset() = landedAtTime != 0L && (timeProvider() - landedAtTime).milliseconds >= resetDuration

    private var lastUpdateTime: Long = 0

    // --- Public API ---

    /**
     * Processes the latest sensor readings and advances the state machine if guard
     * conditions are satisfied.
     *
     * This method calculates the time elapsed since the last call to drive time-based
     * state transitions. It should be called on every sensor tick.
     *
     * To ensure accuracy and prevent jumps:
     * 1. It returns immediately if [climbrate] or [velocity] are at [Float.MIN_VALUE].
     * 2. The first valid call only anchors [lastUpdateTime] and does not advance the machine.
     * 3. Elapsed time is capped at 2 seconds to prevent massive state jumps if the
     *    app was paused or sensors dropped out.
     */
    fun detect() {
        if (climbrate == Float.MIN_VALUE || velocity == Float.MIN_VALUE) {
            return // Not enough data yet; skip until both sensors have reported.
        }

        val currentTime = timeProvider()
        // First call: anchor the clock without advancing the machine to avoid
        // an artificially large elapsed time on the very first tick.
        if (lastUpdateTime == 0L) {
            lastUpdateTime = currentTime
            return
        }

        val maxElapsedTime = 2.seconds
        val deltaMillis = (currentTime - lastUpdateTime).coerceAtLeast(0L)
        val elapsedTime = minOf(deltaMillis.milliseconds, maxElapsedTime)
        lastUpdateTime = currentTime

        Timber.v("AutoStartDetector: Elapsed time: $elapsedTime, climbrate: $climbrate, velocity: $velocity")
        stateMachine.transition(Event.OnUpdate(elapsedTime))
    }

    /** Resets the detector to [Companion.State.WaitForTakeOff] and clears all time accumulators. */
    fun reset() {
        timeInTakeOffCondition = Duration.ZERO
        timeInFlyingCondition = Duration.ZERO
        timeInLandingCondition = Duration.ZERO
        timeInLandedCondition = Duration.ZERO
        landedAtTime = 0L
        lastUpdateTime = 0L
        stateMachine.transition(Event.OnReset)
    }

    /**
     * Manually triggers a take-off, skipping the automatic detection timer.
     *
     * - From [Companion.State.WaitForTakeOff]: transitions directly to [Companion.State.Flying]
     *   and fires [onTakeOff] via [Companion.SideEffect.TakeOff].
     * - From [Companion.State.TakeOff]: transitions to [Companion.State.Flying]
     *   ([onTakeOff] was already fired when entering [Companion.State.TakeOff]).
     * - All other states: no effect.
     */
    fun start() {
        stateMachine.transition(Event.OnManualStart)
    }

    /**
     * Manually triggers a landing, skipping the automatic detection timer.
     *
     * - From [Companion.State.Flying] or [Companion.State.Landing]: transitions directly to
     *   [Companion.State.Landed] and fires [onLanded] via [Companion.SideEffect.Landed].
     * - All other states: no effect.
     */
    fun stop() {
        stateMachine.transition(Event.OnManualStop)
    }

    // --- State Machine ---

    private val stateMachine = StateMachine.create<State, Event, SideEffect> {
        initialState(State.WaitForTakeOff)

        onTransition {
            val valid = it as? StateMachine.Transition.Valid ?: return@onTransition
            if (valid.fromState != valid.toState) {
                Timber.i("onTransition: ${valid.fromState::class.simpleName} -> ${valid.toState::class.simpleName}")
            }
        }

        state<State.WaitForTakeOff> {
            on<Event.OnUpdate> { event ->
                if (doTakeOff()) {
                    transitionTo(State.TakeOff, SideEffect.TakeOff)
                } else {
                    dontTransition(sideEffect = SideEffect.Monitor(event.elapsedTime))
                }
            }
            on<Event.OnManualStart> {
                // Pilot starts manually: fire TakeOff callback and go straight to Flying.
                transitionTo(State.Flying, SideEffect.TakeOff)
            }
            on<Event.OnReset> {
                transitionTo(State.WaitForTakeOff)
            }
        }

        state<State.TakeOff> {
            on<Event.OnUpdate> { event ->
                if (doFlying()) {
                    transitionTo(State.Flying)
                } else {
                    dontTransition(sideEffect = SideEffect.Monitor(event.elapsedTime))
                }
            }
            on<Event.OnManualStart> {
                // TakeOff callback was already fired when entering this state; just advance to Flying.
                transitionTo(State.Flying)
            }
            on<Event.OnReset> {
                transitionTo(State.WaitForTakeOff)
            }
        }

        state<State.Flying> {
            on<Event.OnUpdate> { event ->
                if (doLanding()) {
                    transitionTo(State.Landing)
                } else if (doLanded()) {
                    // Direct transition when velocity drops to zero without a
                    // classic landing approach (e.g. simulator, abrupt stop).
                    transitionTo(State.Landed, SideEffect.Landed)
                } else {
                    dontTransition(sideEffect = SideEffect.Monitor(event.elapsedTime))
                }
            }
            on<Event.OnManualStop> {
                // Pilot stops manually: fire Landed callback and jump straight to Landed.
                transitionTo(State.Landed, SideEffect.Landed)
            }
            on<Event.OnReset> {
                transitionTo(State.WaitForTakeOff)
            }
        }

        state<State.Landing> {
            on<Event.OnUpdate> { event ->
                // Flying takes priority over landed so a brief speed-up during the landing
                // roll returns the machine to Flying rather than completing the landing.
                if (doFlying()) {
                    transitionTo(State.Flying)
                } else if (doLanded()) {
                    transitionTo(State.Landed, SideEffect.Landed)
                } else {
                    dontTransition(sideEffect = SideEffect.Monitor(event.elapsedTime))
                }
            }
            on<Event.OnManualStop> {
                // Pilot stops manually: fire Landed callback and jump straight to Landed.
                transitionTo(State.Landed, SideEffect.Landed)
            }
            on<Event.OnReset> {
                transitionTo(State.WaitForTakeOff)
            }
        }

        state<State.Landed> {
            on<Event.OnUpdate> { event ->
                if (doReset()) {
                    transitionTo(State.WaitForTakeOff)
                } else {
                    dontTransition(sideEffect = SideEffect.Monitor(event.elapsedTime))
                }
            }
            on<Event.OnReset> {
                transitionTo(State.WaitForTakeOff)
            }
        }

        onTransition {
            val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition

            // Clear all accumulators whenever the state actually changes so the
            // next state starts its timer from zero.
            if (validTransition.fromState != validTransition.toState) {
                timeInTakeOffCondition = Duration.ZERO
                timeInFlyingCondition = Duration.ZERO
                timeInLandingCondition = Duration.ZERO
                timeInLandedCondition = Duration.ZERO
                // Anchor the wall-clock when entering Landed; clear it on any other transition
                // so doReset() cannot fire outside of the Landed state.
                landedAtTime = if (validTransition.toState == State.Landed) timeProvider() else 0L
            }

            when (val sideEffect = validTransition.sideEffect) {
                is SideEffect.TakeOff -> onTakeOff()

                is SideEffect.Landed -> onLanded()

                is SideEffect.Monitor -> {
                    // Accumulate time only while the guard condition holds continuously.
                    // Any interruption resets the counter to prevent false positives.
                    when (validTransition.fromState) {
                        State.WaitForTakeOff -> {
                            if (isTakeOffCondition()) {
                                timeInTakeOffCondition += sideEffect.elapsedTime
                            } else {
                                timeInTakeOffCondition = Duration.ZERO
                            }
                        }

                        State.TakeOff -> {
                            if (isFlyingCondition()) {
                                timeInFlyingCondition += sideEffect.elapsedTime
                            } else {
                                timeInFlyingCondition = Duration.ZERO
                            }
                        }

                        State.Flying -> {
                            if (isLandingCondition()) {
                                timeInLandingCondition += sideEffect.elapsedTime
                            } else {
                                timeInLandingCondition = Duration.ZERO
                            }
                            // Also track landed condition so Flying can transition
                            // directly to Landed when velocity drops without a
                            // classic landing approach (fast + sinking).
                            if (isLandedCondition()) {
                                timeInLandedCondition += sideEffect.elapsedTime
                            } else {
                                timeInLandedCondition = Duration.ZERO
                            }
                        }

                        State.Landing -> {
                            // Both flying and landed timers may advance simultaneously
                            // since the machine must choose between two exit transitions.
                            if (isLandedCondition()) {
                                timeInLandedCondition += sideEffect.elapsedTime
                            } else if (isAscentCondition()) {
                                timeInFlyingCondition += sideEffect.elapsedTime
                            } else {
                                timeInLandedCondition = Duration.ZERO
                                timeInFlyingCondition = Duration.ZERO
                            }
                        }

                        // State.Landed: reset timer is wall-clock based (landedAtTime).
                        // doReset() compares timeProvider() against landedAtTime directly,
                        // so no accumulator needs to be updated here regardless of sensor values.
                        State.Landed -> Unit
                    }
                }

                null -> {}
            }
        }
    }

    companion object {

        // --- State Machine Types ---

        /**
         * Represents all possible states of the auto-start detection machine.
         *
         * Sealed to guarantee exhaustive `when` handling.
         */
        sealed class State {
            /** Waiting for the pilot to begin a take-off run. This is the initial state. */
            data object WaitForTakeOff : State()

            /** Take-off conditions are met; [AutoStartDetector.takeOffDuration] timer is running. */
            data object TakeOff : State()

            /** Airborne and flying; monitoring for the start of a landing approach. */
            data object Flying : State()

            /** Landing approach detected; timer running to confirm touch-down. */
            data object Landing : State()

            /**
             * Touch-down confirmed. Machine remains here until [AutoStartDetector.resetDuration]
             * elapses, then returns to [WaitForTakeOff].
             */
            data object Landed : State()
        }

        /**
         * Events that drive the state machine forward.
         *
         * Sealed to guarantee exhaustive `when` handling.
         */
        sealed class Event {
            /**
             * Emitted on every sensor tick.
             *
             * @property elapsedTime Wall-clock time since the previous [OnUpdate] call.
             */
            data class OnUpdate(val elapsedTime: Duration) : Event()

            /** Forces an immediate reset to [State.WaitForTakeOff] and clears all accumulators. */
            data object OnReset : Event()

            /**
             * Manually skips automatic take-off detection and jumps to [State.Flying].
             * Valid in [State.WaitForTakeOff] (fires [SideEffect.TakeOff]) and [State.TakeOff].
             */
            data object OnManualStart : Event()

            /**
             * Manually skips automatic landing detection and jumps to [State.Landed].
             * Valid in [State.Flying] and [State.Landing] (fires [SideEffect.Landed]).
             */
            data object OnManualStop : Event()
        }

        /**
         * Side effects produced by state transitions.
         *
         * Sealed to guarantee exhaustive `when` handling.
         */
        sealed class SideEffect {
            /**
             * Carries the elapsed time so the `onTransition` handler can advance
             * the correct time accumulator.
             *
             * @property elapsedTime Wall-clock time since the previous sensor tick.
             */
            data class Monitor(val elapsedTime: Duration) : SideEffect()

            /** Fired exactly once when the machine enters [State.TakeOff]. */
            data object TakeOff : SideEffect()

            /** Fired exactly once when the machine enters [State.Landed]. */
            data object Landed : SideEffect()
        }
    }
}
