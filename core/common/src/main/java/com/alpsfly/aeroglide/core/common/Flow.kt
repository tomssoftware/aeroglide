package com.alpsfly.aeroglide.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Returns a Flow that emits sequential [size]d chunks of data from the source flow,
 * after transforming them with [transform].
 *
 * The list passed to [transform] is transient and must not be cached.
 *
 * https://stackoverflow.com/questions/70901974/how-can-i-design-a-flow-which-is-a-average-value-of-every-latest-5-data-of-anoth
 */
fun <T, R> Flow<T>.chunked(size: Int, transform: suspend (List<T>) -> R): Flow<R> = flow {
    val cache = ArrayList<T>(size)
    collect {
        cache.add(it)
        if (cache.size == size) {
            emit(transform(cache))
            cache.clear()
        }
    }
}
