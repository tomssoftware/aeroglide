package com.alpsfly.aeroglide.data.util

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