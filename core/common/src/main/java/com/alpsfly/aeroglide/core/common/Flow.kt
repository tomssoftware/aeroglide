package com.alpsfly.aeroglide.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlin.time.Duration

fun <T> Flow<T>.chunked(duration: Duration): Flow<List<T>> = flow {
    val cache = mutableListOf<T>()
    var lastEmitTime = Clock.System.now()
    collect { value ->
        cache.add(value)
        val currentTime = Clock.System.now()
        val elapsedDuration = currentTime - lastEmitTime
        if (elapsedDuration >= duration) {
            emit(ArrayList(cache))  // Emit a copy of the cache
            cache.clear()  // Clear the cache for the next chunk
            lastEmitTime = currentTime  // Reset the last emit time
        }
    }
    if (cache.isNotEmpty()) {  // Emit any remaining items in the cache
        emit(ArrayList(cache))
    }
}