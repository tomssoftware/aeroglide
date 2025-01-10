package com.alpsfly.aeroglide.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

fun <T> Flow<T>.chunked(duration: Duration, timeProvider: TimeProvider = AndroidTimeProvider()): Flow<List<T>> = flow {
    val chunk = mutableListOf<T>()
    var lastEmitTime = timeProvider.currentTimeMillis()
    collect { value ->
        chunk.add(value)
        val currentTime = timeProvider.currentTimeMillis()
        val elapsedDuration = currentTime - lastEmitTime
        if (elapsedDuration >= duration.inWholeMilliseconds) {
            emit(chunk.toList())  // Emit a copy of the cache
            chunk.clear()  // Clear the cache for the next chunk
            lastEmitTime = currentTime  // Reset the last emit time
        }
    }
    if (chunk.isNotEmpty()) {  // Emit any remaining items in the cache
        emit(ArrayList(chunk))
    }
}

fun <T : Number> Flow<T>.movingAverage(windowSize: Int): Flow<Float> = flow {
    val window = ArrayDeque<Float>(windowSize)
    collect { value ->
        if (window.size == windowSize) {
            window.removeFirst()
        }
        window.addLast(value.toFloat())
        val average = window.average().toFloat()
        emit(average)
    }
}

fun <T : Number> Flow<T>.movingAverage(duration: Duration, frequency: Float): Flow<Float> {
    return flow {
        val windowSize = if (frequency > 0f) (duration.inWholeMilliseconds / (1000 / frequency)).toInt() else 1
        val window = ArrayDeque<Float>(windowSize)
        collect { value ->
            if (window.isNotEmpty() && window.size == windowSize) {
                window.removeFirst()
            }
            window.addLast(value.toFloat())
            val average = window.average().toFloat()
            emit(average)
        }
    }
}

