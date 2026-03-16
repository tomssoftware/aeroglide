package com.alpsfly.aeroglide.core.domain.usecase

import android.content.SharedPreferences
import com.alpsfly.aeroglide.core.common.audio.VarioTone
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.model.hardware.Climbrate
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.pow

@OptIn(ExperimentalCoroutinesApi::class)
class VarioToneUseCaseUnitTest {

    private lateinit var sensorRepository: SensorRepository
    private lateinit var varioTone: VarioTone
    private lateinit var prefs: SharedPreferences
    private lateinit var climbrateFlow: MutableSharedFlow<Climbrate>
    private lateinit var useCase: VarioToneUseCase

    private var climbThreshold = 0.2f
    private var sinkThreshold = -0.3f

    @Before
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `enable() activates tone engine and starts collecting`() = runTest {
        createUseCase()

        assertFalse(useCase.isToneEnabled.value)

        useCase.enable()
        advanceUntilIdle()

        assertTrue(useCase.isToneEnabled.value)
        verify(exactly = 1) { prefs.getFloat("spk_vario_tone_threshold_climb", any()) }
        verify(exactly = 1) { prefs.getFloat("spk_vario_tone_threshold_sink", any()) }
        verify(exactly = 0) { varioTone.startClimbTone(any(), any(), any()) }
        verify(exactly = 0) { varioTone.startSinkTone(any()) }
        verify(exactly = 0) { varioTone.stop() }
    }

    @Test
    fun `disable() silences and releases audio backend`() = runTest {
        createUseCase()

        useCase.enable()
        advanceUntilIdle()
        useCase.disable()
        advanceUntilIdle()

        assertFalse(useCase.isToneEnabled.value)
        verify(exactly = 1) { varioTone.shutdown() }

        // Second disable is a no-op.
        useCase.disable()
        advanceUntilIdle()
        verify(exactly = 1) { varioTone.shutdown() }
    }

    @Test
    fun `disable() stops collector so later emissions cannot trigger tones`() = runTest {
        createUseCase()
        useCase.enable()
        emitClimbrate(1.0f)

        useCase.disable()
        advanceUntilIdle()

        emitClimbrate(-2.0f)
        emitClimbrate(2.0f)

        verify(exactly = 1) { varioTone.startClimbTone(any(), any(), any()) }
        verify(exactly = 0) { varioTone.startSinkTone(any()) }
        verify(exactly = 1) { varioTone.shutdown() }
    }

    @Test
    fun `first climb sample above threshold starts climb tone with computed parameters`() = runTest {
        createUseCase()
        val frequencySlot = slot<Float>()
        val beepSlot = slot<Long>()
        val pauseSlot = slot<Long>()

        useCase.enable()
        emitClimbrate(1.2f)

        verify(exactly = 1) {
            varioTone.startClimbTone(
                capture(frequencySlot),
                capture(beepSlot),
                capture(pauseSlot),
            )
        }

        val expected = expectedClimbState(1.2f)
        assertTrue(kotlin.math.abs(frequencySlot.captured - expected.frequencyHz) < 0.001f)
        assertTrue(beepSlot.captured == expected.beepMs)
        assertTrue(pauseSlot.captured == expected.pauseMs)
    }

    @Test
    fun `first sink sample below threshold starts sink tone with computed frequency`() = runTest {
        createUseCase()
        val frequencySlot = slot<Float>()

        useCase.enable()
        emitClimbrate(-2.0f)

        verify(exactly = 1) { varioTone.startSinkTone(capture(frequencySlot)) }

        val expected = expectedSinkFrequency(-2.0f)
        assertTrue(kotlin.math.abs(frequencySlot.captured - expected) < 0.001f)
    }

    @Test
    fun `zone transitions trigger climb sink and stop exactly once each`() = runTest {
        createUseCase()
        useCase.enable()

        emitClimbrate(1.0f)   // Silence -> Climb
        emitClimbrate(-1.0f)  // Climb -> Sink
        emitClimbrate(0.0f)   // Sink -> Silence

        verify(exactly = 1) { varioTone.startClimbTone(any(), any(), any()) }
        verify(exactly = 1) { varioTone.startSinkTone(any()) }
        verify(exactly = 1) { varioTone.stop() }
    }

    @Test
    fun `samples within same climb zone update climb tone parameters immediately`() = runTest {
        createUseCase()
        val frequencies = mutableListOf<Float>()
        val beeps = mutableListOf<Long>()
        val pauses = mutableListOf<Long>()
        useCase.enable()

        emitClimbrate(0.5f)
        emitClimbrate(1.0f)
        emitClimbrate(2.5f)

        verify(exactly = 3) {
            varioTone.startClimbTone(capture(frequencies), capture(beeps), capture(pauses))
        }
        verify(exactly = 0) { varioTone.startSinkTone(any()) }
        verify(exactly = 0) { varioTone.stop() }

        val expectedLast = expectedClimbState(2.5f)
        assertTrue(kotlin.math.abs(frequencies.last() - expectedLast.frequencyHz) < 0.001f)
        assertTrue(beeps.last() == expectedLast.beepMs)
        assertTrue(pauses.last() == expectedLast.pauseMs)
    }

    @Test
    fun `thresholds are read once per activation and reloaded after re-enable`() = runTest {
        climbThreshold = 1.0f
        createUseCase()

        useCase.enable()
        emitClimbrate(0.5f)

        verify(exactly = 0) { varioTone.startClimbTone(any(), any(), any()) }

        // Change settings while active: current collection keeps old snapshot.
        climbThreshold = 0.2f
        emitClimbrate(0.5f)
        verify(exactly = 0) { varioTone.startClimbTone(any(), any(), any()) }

        useCase.disable()
        useCase.enable()
        emitClimbrate(0.5f)

        verify(exactly = 1) { varioTone.startClimbTone(any(), any(), any()) }
        verify(exactly = 2) { prefs.getFloat("spk_vario_tone_threshold_climb", any()) }
        verify(exactly = 2) { prefs.getFloat("spk_vario_tone_threshold_sink", any()) }
    }

    private fun TestScope.createUseCase() {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        climbrateFlow = MutableSharedFlow(extraBufferCapacity = 32)
        sensorRepository = mockk {
            every { climbrateFlowUi } returns climbrateFlow
        }
        varioTone = mockk(relaxed = true)
        prefs = mockk {
            every { getFloat("spk_vario_tone_threshold_climb", any()) } answers { climbThreshold }
            every { getFloat("spk_vario_tone_threshold_sink", any()) } answers { sinkThreshold }
        }

        useCase = VarioToneUseCase(
            sensorRepository = sensorRepository,
            varioTone = varioTone,
            prefs = prefs,
            scope = scope,
        )
    }

    private suspend fun TestScope.emitClimbrate(climbrate: Float) {
        climbrateFlow.emit(Climbrate(climbrate = climbrate))
        advanceUntilIdle()
    }

    private data class ExpectedClimbState(
        val frequencyHz: Float,
        val beepMs: Long,
        val pauseMs: Long,
    )

    private fun expectedClimbState(climbrate: Float): ExpectedClimbState {
        val frequencyHz = (700f * 2f.pow(climbrate / 5f)).coerceIn(700f, 2800f)
        val totalPeriodMs = (600L - (climbrate * 35f).toLong()).coerceIn(120L, 600L)
        val dutyCycle = (0.35f + climbrate * 0.04f).coerceIn(0.35f, 0.70f)
        val beepMs = (totalPeriodMs * dutyCycle).toLong()
        return ExpectedClimbState(
            frequencyHz = frequencyHz,
            beepMs = beepMs,
            pauseMs = totalPeriodMs - beepMs,
        )
    }

    private fun expectedSinkFrequency(climbrate: Float): Float =
        (500f + climbrate * 30f).coerceIn(220f, 500f)
}
