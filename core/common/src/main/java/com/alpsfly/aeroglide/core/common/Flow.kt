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

