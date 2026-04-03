package de.tomssoftware.aeroglide.core.common

import android.os.SystemClock

interface TimeProvider {
    fun nanoTime(): Long
    fun currentTimeMillis(): Long
}

class SystemTimeProvider : TimeProvider {
    override fun nanoTime(): Long {
        return System.nanoTime()
    }
    override fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }
}

class AndroidTimeProvider : TimeProvider {
    override fun nanoTime(): Long {
        return System.nanoTime()
    }
    override fun currentTimeMillis(): Long {
        return SystemClock.elapsedRealtime()
    }
}