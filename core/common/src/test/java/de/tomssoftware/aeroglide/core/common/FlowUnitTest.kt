package de.tomssoftware.aeroglide.core.common

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration


class MockTimeProvider(private var currentMillis: Long = 0) : TimeProvider {

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun advanceTimeBy(millis: Duration) {
        val scheduler = coroutineContext[TestCoroutineScheduler]!!
        scheduler.advanceTimeBy(millis.inWholeMilliseconds)
        currentMillis += millis.inWholeMilliseconds
    }

    override fun nanoTime(): Long {
        return currentMillis.toDuration(DurationUnit.MILLISECONDS).inWholeNanoseconds
    }

    override fun currentTimeMillis(): Long {
        return currentMillis
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
    fun `simple test with logging`() = runTest {

        fun timeBasedFlow(): Flow<Int> = flow {
            repeat(5) {
                delay(1000)
                println("emit $it")
                emit(it)
            }
        }

        val resultFlow = timeBasedFlow().chunked(1000.milliseconds, mockTimeProvider)
        resultFlow.test {
            mockTimeProvider.advanceTimeBy(1000.milliseconds)
            assertEquals(listOf(0), awaitItem())
            println("awaitItem 0")
            mockTimeProvider.advanceTimeBy(1000.milliseconds)
            assertEquals(listOf(1), awaitItem())
            println("awaitItem 1")
            mockTimeProvider.advanceTimeBy(1000.milliseconds)
            assertEquals(listOf(2), awaitItem())
            println("awaitItem 2")
            mockTimeProvider.advanceTimeBy(1000.milliseconds)
            assertEquals(listOf(3), awaitItem())
            println("awaitItem 3")
            mockTimeProvider.advanceTimeBy(1000.milliseconds)
            assertEquals(listOf(4), awaitItem())
            println("awaitItem 4")
            awaitComplete()
            println("awaitComplete")
        }
    }

    @Test
    fun `simple test with logging 2`() = runTest {

        fun timeBasedFlow(): Flow<Int> = flow {
            repeat(2) {
                delay(1000)
                println("emit $it")
                emit(it)
            }
        }

        val resultFlow = timeBasedFlow().chunked(1000.milliseconds, mockTimeProvider)
        resultFlow.test {
            println("mockTimeProvider.advanceTimeBy(1000.milliseconds)")
            mockTimeProvider.advanceTimeBy(1000.milliseconds)
            assertEquals(listOf(0), awaitItem())
            println("mockTimeProvider.advanceTimeBy(1000.milliseconds)")
            mockTimeProvider.advanceTimeBy(1000.milliseconds)
            assertEquals(listOf(1), awaitItem())

            awaitComplete()
        }
    }

    @Test
    fun `chunked should emit empty list when source flow is empty`() = runTest {
        val sourceFlow = flow<Int> {
            mockTimeProvider.advanceTimeBy(100.milliseconds)
        }
        val resultFlow = sourceFlow.chunked(100.milliseconds, mockTimeProvider)
        resultFlow.test {
            //expectNoEvents()
            awaitComplete()
        }
    }

    @Test
    fun `chunked should emit single chunk when source flow emits within duration`() = runTest {
        val sourceFlow = flow {
            println("delay 1")
            delay(10)
            println("emit 1")
            emit(1)

            println("delay 2")
            delay(500)
            println("emit 2")
            emit(2)

            println("delay 3")
            delay(50)
            println("emit 3")
            emit(3)
        }
        val resultFlow = sourceFlow.chunked(100.milliseconds, mockTimeProvider)
        resultFlow.test {
            println("mockTimeProvider.advanceTimeBy(510.milliseconds)")
            mockTimeProvider.advanceTimeBy(510.milliseconds)
            println("awaitItem @ ${mockTimeProvider.currentTimeMillis()}")
            assertEquals(listOf(1, 2), awaitItem())

            println("mockTimeProvider.advanceTimeBy(50.milliseconds)")
            mockTimeProvider.advanceTimeBy(50.milliseconds)
            println("awaitItem @ ${mockTimeProvider.currentTimeMillis()}")
            assertEquals(listOf(3), awaitItem())

            println("awaitComplete")
            awaitComplete()
        }
    }

    @Test
    fun `chunked should emit multiple chunks when source flow emits over duration`() = runTest {
        val sourceFlow = flow {
            delay(50)
            emit(1)
            delay(50)
            emit(2)
            delay(100)
            emit(3)
            delay(100)
            emit(4)
        }
        val resultFlow = sourceFlow.chunked(100.milliseconds, mockTimeProvider)
        resultFlow.test {
            mockTimeProvider.advanceTimeBy(100.milliseconds)
            assertEquals(listOf(1, 2), awaitItem())
            mockTimeProvider.advanceTimeBy(100.milliseconds)
            assertEquals(listOf(3), awaitItem())
            mockTimeProvider.advanceTimeBy(100.milliseconds)
            assertEquals(listOf(4), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `chunked should emit remaining items in last chunk`() = runTest {
        val sourceFlow = flow {
            delay(1000)
            emit(1)
            delay(2000)
            emit(2)
            delay(1000)
            emit(3)
        }
        val resultFlow = sourceFlow.chunked(3000.milliseconds, mockTimeProvider)
        resultFlow.test {
            mockTimeProvider.advanceTimeBy(3000.milliseconds)
            assertEquals(listOf(1, 2), awaitItem())
            mockTimeProvider.advanceTimeBy(3000.milliseconds)
            assertEquals(listOf(3), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `chunked should work correctly with zero duration`() = runTest {

        val sourceFlow = flow {
            emit(1)
            emit(2)
            emit(3)
        }
        val resultFlow = sourceFlow.chunked(0.milliseconds, mockTimeProvider)
        resultFlow.test {
            assertEquals(listOf(1), awaitItem())
            assertEquals(listOf(2), awaitItem())
            assertEquals(listOf(3), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `chunked should work correctly with a very long duration`() = runTest {

        val sourceFlow = flow {
            delay(50)
            emit(1)
            delay(100)
            emit(2)
            delay(100)
            emit(3)
        }
        val resultFlow = sourceFlow.chunked(1000.milliseconds, mockTimeProvider)
        resultFlow.test {
            mockTimeProvider.advanceTimeBy(1000.milliseconds)
            assertEquals(listOf(1, 2, 3), awaitItem())
            awaitComplete()
        }
    }
}
