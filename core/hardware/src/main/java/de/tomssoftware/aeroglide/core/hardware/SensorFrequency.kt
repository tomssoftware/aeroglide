package de.tomssoftware.aeroglide.core.hardware

import de.tomssoftware.aeroglide.core.common.SystemTimeProvider
import de.tomssoftware.aeroglide.core.common.TimeProvider
import kotlin.time.Duration.Companion.seconds

class SensorFrequency(
    private var startTime: Long = 0,
    private var count: Long = 0L,
    private var frequency: Float = 0f,
    private val timeProvider: TimeProvider = SystemTimeProvider()
) {
    fun inc(): Float {
        if (startTime == 0L) {
            startTime = timeProvider.nanoTime()
        }
        if (count == Long.MAX_VALUE) {
            count = 0
            startTime = timeProvider.nanoTime()
        }

        val duration = timeProvider.nanoTime() - startTime
        frequency = if (duration >= 1.seconds.inWholeNanoseconds) {
            count / (duration / 1.seconds.inWholeNanoseconds).toFloat()
        } else {
            0f
        }
        count++

        return frequency
    }

    fun get(): Float {
        return frequency
    }
}

