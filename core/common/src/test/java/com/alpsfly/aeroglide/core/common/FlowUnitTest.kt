package com.alpsfly.aeroglide.core.common

import app.cash.turbine.test
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration


class MockTimeProvider(private var currentMillis: Long = 0) : TimeProvider {

    fun advanceTimeBy(millis: Duration) {
        currentMillis += millis.inWholeMilliseconds
    }

    override fun nanoTime(): Long {
        return currentMillis.toDuration(DurationUnit.MILLISECONDS).inWholeNanoseconds
    }
}

class FlowChunkedTest {

    private lateinit var mockTimeProvider: MockTimeProvider

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mockTimeProvider = MockTimeProvider()
    }

    @Test
    fun `chunked should emit empty list when source flow is empty`() = runTest {
        val sourceFlow = flow<Int> {
            mockTimeProvider.advanceTimeBy(100.milliseconds)
        }
        val resultFlow = sourceFlow.chunked(100.milliseconds)
        resultFlow.test {
            //expectNoEvents() ???
            awaitComplete()
        }
    }

    @Test
    fun `chunked should emit single chunk when source flow emits within duration`() = runTest {
        val sourceFlow = flow {
            emit(1)
            mockTimeProvider.advanceTimeBy(50.milliseconds)
            emit(2)
            mockTimeProvider.advanceTimeBy(25.milliseconds)
            emit(3)
        }
        val resultFlow = sourceFlow.chunked(100.milliseconds, mockTimeProvider)
        resultFlow.test {
            val chunk1 = awaitItem()
            assertEquals(listOf(1, 2, 3), chunk1)
            awaitComplete()
        }
    }

    @Test
    fun `chunked should emit multiple chunks when source flow emits over duration`() = runTest { // ???
        val sourceFlow = flow {
            emit(1)
            mockTimeProvider.advanceTimeBy(50.milliseconds)
            emit(2)
            mockTimeProvider.advanceTimeBy(100.milliseconds)
            emit(3)
            mockTimeProvider.advanceTimeBy(100.milliseconds)
            emit(4)
        }
        val resultFlow = sourceFlow.chunked(40.milliseconds, mockTimeProvider)
        resultFlow.test {
            assertEquals(listOf(1, 2), awaitItem())
            assertEquals(listOf(3), awaitItem())
            assertEquals(listOf(4), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `chunked should emit remaining items in last chunk`() = runTest { // ????
        val sourceFlow = flow {
            emit(1)
            mockTimeProvider.advanceTimeBy(2000.milliseconds)
            emit(2)
            mockTimeProvider.advanceTimeBy(3000.milliseconds)
            emit(3)
        }
        val resultFlow = sourceFlow.chunked(500.milliseconds, mockTimeProvider)
        resultFlow.test {
            assertEquals(listOf(1, 2), awaitItem())
            assertEquals(listOf(3), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `chunked should work correctly with zero duration`() = runTest {

        val sourceFlow = flow {
            emit(1)
            mockTimeProvider.advanceTimeBy(0.milliseconds)
            emit(2)
            mockTimeProvider.advanceTimeBy(0.milliseconds)
            emit(3)
        }
        val resultFlow = sourceFlow.chunked(0.milliseconds, mockTimeProvider)
        resultFlow.test {
            val chunk1 = awaitItem()
            assertEquals(listOf(1), chunk1)
            val chunk2 = awaitItem()
            assertEquals(listOf(2), chunk2)
            val chunk3 = awaitItem()
            assertEquals(listOf(3), chunk3)
            awaitComplete()
        }
    }

    @Test
    fun `chunked should work correctly with a very long duration`() = runTest {

        val sourceFlow = flow {
            emit(1)
            mockTimeProvider.advanceTimeBy(50.milliseconds)
            emit(2)
            mockTimeProvider.advanceTimeBy(100.milliseconds)
            emit(3)
        }
        val resultFlow = sourceFlow.chunked(1000.milliseconds, mockTimeProvider)
        resultFlow.test {
            val chunk1 = awaitItem()
            assertEquals(listOf(1, 2, 3), chunk1)
            awaitComplete()
        }
    }
}
