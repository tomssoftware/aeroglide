package com.alpsfly.aeroglide.core.hardware

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
        frequency = count++ / ((timeProvider.nanoTime() - startTime) / 1.seconds.inWholeNanoseconds).toFloat()
        return frequency
    }

    fun get(): Float {
        return frequency
    }
}

interface TimeProvider {
    fun nanoTime(): Long
}

class SystemTimeProvider : TimeProvider {
    override fun nanoTime(): Long {
        return System.nanoTime()
    }
}
