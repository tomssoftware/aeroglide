package com.alpsfly.aeroglide.core.hardware

class SensorFrequency(private var startTime: Long = 0, private var count: Long = 0L, private val timeProvider: TimeProvider = SystemTimeProvider()) {

    fun inc(): Float {
        if (startTime == 0L) {
            startTime = timeProvider.nanoTime()
        }
        if (count == Long.MAX_VALUE) {
            count = 0
            startTime = timeProvider.nanoTime()
        }
        return getFrequency(startTime, count++)
    }

    fun get(): Float {
        return getFrequency(startTime, count)
    }

    private fun getFrequency(startTime: Long, count: Long): Float {
        val now = timeProvider.nanoTime()
        return (count / ((now - startTime) / 1000000000.0f))
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
