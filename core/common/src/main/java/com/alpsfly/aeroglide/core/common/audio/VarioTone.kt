package com.alpsfly.aeroglide.core.common.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

// PCM sample rate used for all generated tones.
private const val SAMPLE_RATE = 44100

// Duration of each PCM chunk written to the stream track.
// Coroutine cancellation is checked after every write; latency ≤ CHUNK_MS.
private const val CHUNK_MS = 50L

/**
 * Generates and plays audio tones for the variometer.
 *
 * Supports two tone modes:
 * - **Climb**: intermittent beep with configurable frequency and duty cycle.
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

    /** Stops any beep started by [playBeep]. Delegates to [stop]. */
    fun stopBeep()

    // --- New API ---

    /**
     * Starts a pulsed climb tone that loops until [stop] is called:
     * audible beep for [beepMs] ms, then silence for [pauseMs] ms, then repeat.
     *
     * Any previously playing tone is cancelled first.
     *
     * @param frequencyHz Tone frequency in Hz (typical vario climb range: 700–2200 Hz).
     * @param beepMs      Duration of the audible beep per cycle in milliseconds.
     * @param pauseMs     Duration of silence between beeps in milliseconds.
     */
    fun startClimbTone(frequencyHz: Float, beepMs: Long, pauseMs: Long)

    /**
     * Starts a continuous sink tone at [frequencyHz] and loops until [stop] is called.
     *
     * Any previously playing tone is cancelled first.
     *
     * @param frequencyHz Tone frequency in Hz (typical vario sink range: 220–500 Hz).
     */
    fun startSinkTone(frequencyHz: Float)

    /**
     * Cancels any currently playing tone and releases all audio resources.
     *
     * Safe to call when no tone is playing (no-op). For the stream-based sink tone,
     * audio stops within at most [CHUNK_MS] ms because cancellation is checked after
     * each [AudioTrack.write] call.
     */
    fun stop()
}

/**
 * Coroutine-based implementation of [VarioTone] backed by Android's [AudioTrack].
 *
 * Uses [AudioAttributes.USAGE_ASSISTANCE_SONIFICATION] so the system treats the tone
 * as a navigational aid rather than media – this prevents music apps from ducking or
 * muting a safety-relevant vario signal.
 *
 * Climb tones use [AudioTrack.MODE_STATIC] (short, one-shot buffers).
 * Sink tones use [AudioTrack.MODE_STREAM] (continuous write loop on [Dispatchers.IO]).
 *
 * @param scope [CoroutineScope] that owns the playback coroutines.
 *              Inject the app-scoped coroutine scope (`@ApplicationScope`) so tones
 *              survive Activity recreation during a flight.
 */
class VarioToneImpl(private val scope: CoroutineScope) : VarioTone {

    // --- Legacy state ---

    private var legacyFrequency: Float = 440f
    private var legacyDurationMs: Long = 1000L

    // --- Active playback ---

    // At most one job is active at a time; start* cancels the previous one before launching.
    private var job: Job? = null

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
        stop()
        job = scope.launch {
            while (isActive) {
                val track = buildStaticTrack(frequencyHz, beepMs)
                try {
                    track.play()
                    // delay() is a cooperative cancellation point – CancellationException
                    // propagates to the finally block which releases the track.
                    delay(beepMs)
                } finally {
                    track.stop()
                    track.release()
                }
                if (pauseMs > 0 && isActive) delay(pauseMs)
            }
        }
    }

    override fun startSinkTone(frequencyHz: Float) {
        stop()
        // Dispatchers.IO: AudioTrack.write() is a blocking JVM call and must not
        // run on Dispatchers.Default to avoid starving the shared thread pool.
        job = scope.launch(Dispatchers.IO) {
            val chunkSamples = (SAMPLE_RATE * CHUNK_MS / 1000).toInt()
            val minBuf = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val track = buildStreamTrack(maxOf(minBuf, chunkSamples * Short.SIZE_BYTES))
            try {
                track.play()
                val buffer = generateSine(frequencyHz, chunkSamples)
                // Cancellation is checked at each isActive; maximum stop latency = CHUNK_MS.
                while (isActive) {
                    track.write(buffer, 0, buffer.size)
                }
            } finally {
                // Guaranteed cleanup on cancellation, exception, or normal exit.
                track.stop()
                track.release()
            }
        }
    }

    override fun stop() {
        // cancel() is non-blocking; the coroutine's finally block handles AudioTrack cleanup.
        job?.cancel()
        job = null
    }

    // --- Private helpers ---

    private fun audioAttributes(): AudioAttributes =
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

    private fun audioFormat(): AudioFormat =
        AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(SAMPLE_RATE)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()

    /**
     * Builds a [AudioTrack.MODE_STATIC] track pre-loaded with a [frequencyHz] sine wave
     * of [durationMs] milliseconds.
     *
     * MODE_STATIC is appropriate for short, one-shot beeps (< 1 second).
     * The caller must call [AudioTrack.stop] and [AudioTrack.release] after use.
     */
    private fun buildStaticTrack(frequencyHz: Float, durationMs: Long): AudioTrack {
        val numSamples = (durationMs * SAMPLE_RATE / 1000).toInt()
        val buffer = generateSine(frequencyHz, numSamples)
        return AudioTrack.Builder()
            .setAudioAttributes(audioAttributes())
            .setAudioFormat(audioFormat())
            .setBufferSizeInBytes(buffer.size * Short.SIZE_BYTES)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
            .also { it.write(buffer, 0, buffer.size) }
    }

    /**
     * Builds a [AudioTrack.MODE_STREAM] track for continuous PCM output.
     *
     * The caller is responsible for feeding data via [AudioTrack.write] in a loop
     * and must call [AudioTrack.stop] and [AudioTrack.release] when done.
     *
     * @param bufferSizeInBytes Internal ring buffer size; must be ≥ [AudioTrack.getMinBufferSize].
     */
    private fun buildStreamTrack(bufferSizeInBytes: Int): AudioTrack =
        AudioTrack.Builder()
            .setAudioAttributes(audioAttributes())
            .setAudioFormat(audioFormat())
            .setBufferSizeInBytes(bufferSizeInBytes)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

    /**
     * Generates a full-scale 16-bit PCM sine wave.
     *
     * @param frequencyHz Frequency of the tone in Hz.
     * @param numSamples  Number of PCM samples to produce.
     * @return [ShortArray] of [numSamples] signed 16-bit samples.
     */
    private fun generateSine(frequencyHz: Float, numSamples: Int): ShortArray =
        ShortArray(numSamples) { i ->
            (sin(2.0 * PI * frequencyHz * i / SAMPLE_RATE) * Short.MAX_VALUE).toInt().toShort()
        }
}