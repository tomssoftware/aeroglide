package de.tomssoftware.aeroglide.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
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


/**
 * A generic extension function that checks a Flow for out-of-range values.
 * It uses a predicate to check the value and throttles logging to once per flow collection.
 *
 * @param T The type of data in the Flow.
 * @param predicate A function that takes a value of type T and returns `true` if it is valid, `false` if it is an anomaly.
 * @param onDeviation A function to be called when an anomaly is detected for the first time.
 */
fun <T> Flow<T>.logDeviation(
    predicate: (T) -> Boolean,
    onDeviation: (T) -> Unit
): Flow<T> {
    var anomalyReported = false // State is now encapsulated inside the function
    return this.onEach { value ->
        if (!anomalyReported && !predicate(value)) {
            // If not yet reported AND the predicate fails (value is invalid)...
            onDeviation(value)
            anomalyReported = true // Mark as reported for this flow's lifecycle
        }
    }
}

