package com.alpsfly.aeroglide.core.data.util

import timber.log.Timber

class SensorFrequency(private var startTime: Long = 0, private var count: Long = 0L) {

    init {
        Timber.v("SensorFrequency init")
    }

    fun get(): Float {
        if (startTime == 0L) {
            startTime = System.nanoTime()
        }
        return getFrequency(startTime, count++)
    }

    private fun getFrequency(startTime: Long, count: Long): Float {
        val now = System.nanoTime()
        return (count / ((now - startTime) / 1000000000.0f))
    }
}