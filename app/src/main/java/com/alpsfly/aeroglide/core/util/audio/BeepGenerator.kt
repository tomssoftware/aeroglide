package com.alpsfly.aeroglide.core.util.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin

interface BeepGenerator {
    fun setFrequency(frequency: Float)
    fun setDuration(durationMillis: Long)
    fun playBeep()
    fun stopBeep()
}

class BeepGeneratorImpl : BeepGenerator {

    private var frequency: Float = 440f // Default frequency
    private var durationMillis: Long = 1000L // Default duration
    private var audioTrack: AudioTrack? = null

    override fun setFrequency(frequency: Float) {
        this.frequency = frequency
    }

    override fun setDuration(durationMillis: Long) {
        this.durationMillis = durationMillis
    }

    override fun playBeep() {
        val sampleRate = 44100
        val numSamples = (durationMillis * sampleRate / 1000).toInt()
        val samples = DoubleArray(numSamples)
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            samples[i] = sin(2 * PI * frequency * i / sampleRate)
            buffer[i] = (samples[i] * Short.MAX_VALUE).toInt().toShort()
        }

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack?.write(buffer, 0, buffer.size)
        audioTrack?.play()
    }

    override fun stopBeep() {
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }
}