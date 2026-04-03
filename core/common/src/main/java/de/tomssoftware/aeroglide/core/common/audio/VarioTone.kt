package de.tomssoftware.aeroglide.core.common.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

// ─── Audio constants ──────────────────────────────────────────────────────────

private const val SAMPLE_RATE = 44100

// Chunk written per loop iteration: 20 ms → mode changes take effect within one chunk.
private const val CHUNK_SAMPLES = SAMPLE_RATE / 50

// Linear fade-in/out window at beep boundaries to prevent audible clicks.
private const val FADE_SAMPLES = SAMPLE_RATE * 5 / 1000   // 5 ms

private const val TWO_PI = 2.0 * PI

// 80 % of full scale – headroom to avoid clipping at Short.MAX_VALUE.
private const val AMPLITUDE = Short.MAX_VALUE * 0.8

// ─── Internal stream state ────────────────────────────────────────────────────

/** Modes for the single persistent audio stream. */
private enum class StreamMode { SILENCE, CLIMB, SINK, STOPPING }

// ─── Public interface ─────────────────────────────────────────────────────────

/**
 * Generates and plays variometer audio tones.
 *
 * Supports two tone modes:
 * - **Climb**: pulsed beep with configurable frequency and duty cycle.
 * - **Sink**: continuous tone whose frequency decreases with sink rate.
 *
 * All public methods are safe to call from any thread or coroutine context.
 */
interface VarioTone {

    // --- Legacy API (kept for backwards compatibility) ---

    /** Sets the base frequency in Hz used by [playBeep]. */
    fun setFrequency(frequency: Float)

    /** Sets the beep duration in milliseconds used by [playBeep]. */
    fun setDuration(durationMillis: Long)

    /**
     * Plays a single pulsed beep using the values last set by [setFrequency] and [setDuration].
     * Delegates to [startClimbTone] with equal beep and pause durations.
     */
    fun playBeep()

    /** Silences the current tone. Delegates to [stop]. */
    fun stopBeep()

    // --- New API ---

    /**
     * Starts a pulsed climb tone that loops until [stop] is called:
     * audible beep for [beepMs] ms, then silence for [pauseMs] ms, then repeat.
     *
     * If the stream is already running the mode switches seamlessly within one 20 ms
     * chunk – no AudioTrack restart, no click.
     *
     * @param frequencyHz Tone frequency in Hz (typical vario climb range: 700–2200 Hz).
     * @param beepMs      Duration of the audible beep per cycle in milliseconds.
     * @param pauseMs     Duration of silence between beeps in milliseconds.
     */
    fun startClimbTone(frequencyHz: Float, beepMs: Long, pauseMs: Long)

    /**
     * Starts a continuous sink tone at [frequencyHz].
     *
     * If the stream is already running the mode switches seamlessly within one 20 ms chunk.
     *
     * @param frequencyHz Tone frequency in Hz (typical vario sink range: 220–500 Hz).
     */
    fun startSinkTone(frequencyHz: Float)

    /**
     * Silences the tone but keeps the audio stream alive.
     *
     * For a climb tone that is currently beeping, the current beep plays to completion
     * (including its fade-out) before silence begins – no abrupt cut-off.
     * The stream continues writing silence so that the next [startClimbTone] /
     * [startSinkTone] call resumes without AudioTrack startup latency.
     *
     * Call [shutdown] instead if the audio output should be released entirely.
     */
    fun stop()

    /**
     * Completely stops the audio stream and releases the [AudioTrack].
     *
     * Should be called when the user explicitly disables the vario tone via the UI.
     * After [shutdown] the next [startClimbTone] / [startSinkTone] call will create
     * a fresh stream (with a small startup latency of ~20 ms).
     */
    fun shutdown()
}

// ─── Implementation ───────────────────────────────────────────────────────────

/**
 * Single-stream implementation of [VarioTone] backed by Android's [AudioTrack].
 *
 * ## Design: always-running stream (mirrors the original C++ AAudio implementation)
 *
 * A new [AudioTrack] in `MODE_STREAM` is opened when the first tone is requested and
 * keeps running for the lifetime of the active session.  `track.write()` is called in
 * every loop iteration regardless of the current mode:
 *
 * - **CLIMB / SINK** → buffer filled with the appropriate waveform.
 * - **SILENCE / STOPPING** → buffer filled with zeros.
 *
 * This eliminates every source of audible clicks:
 * - No AudioTrack start/stop between beep cycles.
 * - No phase discontinuity at chunk boundaries (phase counter is continuous).
 * - 5 ms linear **fade-in / fade-out envelope** at beep boundaries.
 * - STOPPING mode lets the current beep complete its natural fade-out before
 *   transitioning to silence – the stream never cuts mid-beep.
 *
 * ## Battery
 * The AudioTrack DAC stays active while the stream is running.  Power consumption is
 * comparable to the original C++ `AAudio` stream and negligible versus GPS / barometer.
 * The stream is released completely when the user disables the tone via [shutdown].
 *
 * @param scope [CoroutineScope] that owns the audio write coroutine.
 *              Use `@ApplicationScope` so the stream survives Activity recreation.
 */
class VarioToneImpl(private val scope: CoroutineScope) : VarioTone {

    private class ToneState {
        var phase = 0.0
        var beepRemaining = 0L
        var pauseRemaining = 0L
        var samplesFromBeepStart = 0L
    }

    // --- Legacy state ---
    private var legacyFrequency = 440f
    private var legacyDurationMs = 1000L

    // --- Stream ---
    // One persistent job keeps the AudioTrack write loop alive until shutdown().
    private var streamJob: Job? = null

    // --- Tone parameters ---
    // Written by the public API (any thread), read by the stream coroutine.
    // @Volatile ensures visibility; no compound atomicity required.
    @Volatile
    private var streamMode = StreamMode.SILENCE
    @Volatile
    private var targetFreq = 440f
    @Volatile
    private var beepSamples = 0L
    @Volatile
    private var pauseSamples = 0L

    // --- Legacy API ---

    override fun setFrequency(frequency: Float) {
        legacyFrequency = frequency
    }

    override fun setDuration(durationMillis: Long) {
        legacyDurationMs = durationMillis
    }

    override fun playBeep() = startClimbTone(legacyFrequency, legacyDurationMs, legacyDurationMs)
    override fun stopBeep() = stop()

    // --- New API ---

    override fun startClimbTone(frequencyHz: Float, beepMs: Long, pauseMs: Long) {
        targetFreq = frequencyHz
        beepSamples = beepMs * SAMPLE_RATE / 1000
        pauseSamples = pauseMs * SAMPLE_RATE / 1000
        streamMode = StreamMode.CLIMB
        ensureStreamRunning()
    }

    override fun startSinkTone(frequencyHz: Float) {
        targetFreq = frequencyHz
        streamMode = StreamMode.SINK
        ensureStreamRunning()
    }

    override fun stop() {
        when (streamMode) {
            StreamMode.CLIMB -> streamMode = StreamMode.STOPPING  // finish beep gracefully
            StreamMode.STOPPING -> Unit                               // already finishing
            else -> streamMode = StreamMode.SILENCE    // immediate silence
        }
        // Stream keeps running – write loop fills the buffer with zeros.
    }

    override fun shutdown() {
        // User explicitly disabled the tone: release AudioTrack entirely.
        streamMode = StreamMode.SILENCE
        streamJob?.cancel()
        streamJob = null
    }

    // --- Stream management ---

    private fun ensureStreamRunning() {
        if (streamJob?.isActive == true) return
        streamJob = scope.launch(Dispatchers.IO) { runStream() }
    }

    /**
     * Opens one [AudioTrack] in `MODE_STREAM` and writes 20 ms PCM chunks in a loop
     * until the coroutine is cancelled by [shutdown].
     *
     * Mode and frequency changes take effect at the next chunk boundary (≤ 20 ms)
     * without restarting the track.
     */
    private suspend fun runStream() {
        val minBuf = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        val track = AudioTrack.Builder()
            .setAudioAttributes(buildAudioAttributes())
            .setAudioFormat(buildAudioFormat())
            // 4 × chunk ring-buffer: absorbs scheduler jitter without adding latency.
            .setBufferSizeInBytes(maxOf(minBuf, CHUNK_SAMPLES * Short.SIZE_BYTES * 4))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        try {
            track.play()

            val buf = ShortArray(CHUNK_SAMPLES)
            // ── Climb tone state machine (local to this coroutine) ──────────────
            val toneState = ToneState()

            // Detect mode transitions so we can reset the state machine on CLIMB entry.
            var lastMode = StreamMode.SILENCE

            while (currentCoroutineContext().isActive) {
                // Snapshot volatile fields once per chunk.
                val mode = streamMode
                val freq = targetFreq
                val phaseInc = TWO_PI * freq / SAMPLE_RATE

                // Reset the climb state machine whenever we (re-)enter CLIMB mode so
                // leftover counters from a previous cycle never cause a spurious pause.
                if (mode == StreamMode.CLIMB && lastMode != StreamMode.CLIMB) {
                    toneState.beepRemaining = 0L
                    toneState.pauseRemaining = 0L
                    toneState.samplesFromBeepStart = 0L
                }
                lastMode = mode

                for (i in buf.indices) {
                    val sample: Short = when (mode) {

                        // ── Continuous sink tone ──────────────────────────────────
                        StreamMode.SINK -> nextSinkSample(toneState)

                        // ── Pulsed climb tone ─────────────────────────────────────
                        StreamMode.CLIMB -> nextClimbSample(toneState)

                        // ── Graceful stop ─────────────────────────────────────────
                        // Finish the current beep with its fade-out, then switch to
                        // SILENCE. The stream keeps running – no break, no cancel.
                        StreamMode.STOPPING -> nextStoppingSample(toneState)

                        // ── Silence ───────────────────────────────────────────────
                        // Buffer filled with zeros: stream stays alive, DAC stays warm.
                        StreamMode.SILENCE -> 0
                    }

                    buf[i] = sample

                    // Phase always advances – including during silence and pause –
                    // so there is never a phase discontinuity when a tone resumes.
                    toneState.phase += phaseInc
                    if (toneState.phase >= TWO_PI) toneState.phase -= TWO_PI
                }

                // Always write – even when the buffer contains only zeros.
                // This keeps the AudioTrack ring-buffer fed and prevents underruns.
                track.write(buf, 0, buf.size)
            }
        } finally {
            track.stop()
            track.release()
        }
    }

    // --- Private helpers ---

    private fun nextClimbSample(state: ToneState): Short {
        if (state.beepRemaining <= 0L && state.pauseRemaining <= 0L) {
            // Start a new beep cycle with a clean phase so the beep starts at zero crossing.
            state.phase = 0.0
            state.beepRemaining = beepSamples
            state.samplesFromBeepStart = 0L
        }

        if (state.beepRemaining > 0L) {
            state.beepRemaining--
            state.samplesFromBeepStart++

            val envelope = computeEnvelope(state.samplesFromBeepStart, state.beepRemaining)
            if (state.beepRemaining == 0L) state.pauseRemaining = pauseSamples

            // Half-rectified sine gives the characteristic vario "blip".
            val raw = sin(state.phase)
            return (maxOf(0.0, raw) * AMPLITUDE * envelope).toInt().toShort()
        }

        if (state.pauseRemaining > 0L) state.pauseRemaining--
        return 0
    }

    private fun nextStoppingSample(state: ToneState): Short {
        if (state.beepRemaining > 0L) {
            state.beepRemaining--
            state.samplesFromBeepStart++

            val envelope = computeEnvelope(state.samplesFromBeepStart, state.beepRemaining)
            if (state.beepRemaining == 0L) {
                // Beep finished: skip pause and switch to silence immediately.
                state.pauseRemaining = 0L
                streamMode = StreamMode.SILENCE
            }

            val raw = sin(state.phase)
            return (maxOf(0.0, raw) * AMPLITUDE * envelope).toInt().toShort()
        }

        // In pause or idle: go silent immediately.
        state.beepRemaining = 0L
        state.pauseRemaining = 0L
        streamMode = StreamMode.SILENCE
        return 0
    }

    private fun nextSinkSample(state: ToneState): Short {
        return (sin(state.phase) * AMPLITUDE).toInt().toShort()
    }

    private fun computeEnvelope(samplesFromBeepStart: Long, beepRemaining: Long): Float = when {
        beepSamples <= 2L * FADE_SAMPLES -> 1f
        samplesFromBeepStart <= FADE_SAMPLES -> samplesFromBeepStart.toFloat() / FADE_SAMPLES
        beepRemaining < FADE_SAMPLES -> beepRemaining.toFloat() / FADE_SAMPLES
        else -> 1f
    }

    private fun buildAudioAttributes(): AudioAttributes =
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

    private fun buildAudioFormat(): AudioFormat =
        AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(SAMPLE_RATE)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
}
