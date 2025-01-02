package com.alpsfly.aeroglide.core.common.units

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class FlowUnitTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `chunked with dynamic size should emit chunks of the specified size`() = runTest {
        val sourceFlow = flow {
            emit(1)
            emit(2)
            emit(3)
            emit(4)
            emit(5)
            emit(6)
            emit(7)
        }
        var size = 1
        val resultFlow = sourceFlow.scan(0) { accumulator, value ->
            if (value % 2 == 0) {
                1
            } else {
                1
            }
        }.flatMapConcat { x ->
            if (x == 0) {
                emptyFlow<Int>()
            } else {
                emptyFlow<Float>()
            }
        }

        val result = resultFlow.toList()
        Assert.assertEquals(listOf(1, 5, 22), result)
    }
}