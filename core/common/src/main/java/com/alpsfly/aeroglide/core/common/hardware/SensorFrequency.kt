package com.alpsfly.aeroglide.core.common.hardware

import timber.log.Timber

class SensorFrequency(private val startTime: Long = System.nanoTime(), private var count: Long = 0L) {

    init {
        Timber.v("SensorFrequency init")
    }

    fun get(): Float {
        return getFrequency(startTime, count++)
    }

    private fun getFrequency(startTime: Long, count: Long): Float {
        val now = System.nanoTime()
        return (count / ((now - startTime) / 1000000000.0f))
    }
}

class DeltaTime(private var time: Long = 0L) {
    fun isValid(): Boolean = (time > 0)

    fun delta(): Float {
        return (System.nanoTime() - time) / 1000000000.0f
    }

    fun update(emissionTime: Long) {
        time = emissionTime
    }
}