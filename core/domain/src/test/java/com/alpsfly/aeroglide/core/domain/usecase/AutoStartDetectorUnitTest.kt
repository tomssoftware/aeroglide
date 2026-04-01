package com.alpsfly.aeroglide.core.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class AutoStartDetectorUnitTest {

    private lateinit var autoStartDetector: AutoStartDetector

    // controllable test clock (milliseconds)
    private var testTimeMillis: Long = 0L

    // Mock the callbacks to verify they are called
    private var takeOffCalled = false
    private var landedCalled = false

    @Before
    fun setUp() {
        autoStartDetector = AutoStartDetector(timeProvider = { testTimeMillis })
        takeOffCalled = false
        landedCalled = false

        // Assign mock implementations to the callbacks
        autoStartDetector.onTakeOff = {
            takeOffCalled = true
        }
        autoStartDetector.onLanded = {
            landedCalled = true
        }
    }

    @Test
    fun `initial state is WaitForTakeOff`() {
        // The detector is initialized in setUp(), no action needed
        // This is an implicit test of the initial state, verified by other tests
    }

    @Test
    fun `detect() does not trigger take off before conditions are met for enough time`() = runTest {
        // GIVEN: Take-off conditions
        autoStartDetector.velocity = autoStartDetector.velocityFlying + 1.0f
        autoStartDetector.climbrate = autoStartDetector.climbrateTakeOff + 0.5f

        // WHEN: detect() is called for less than the required 5 seconds
        autoStartDetector.simulateTimePassing(autoStartDetector.takeOffDuration - 1.seconds)

        // THEN: The onTakeOff callback should NOT have been called
        assertFalse("onTakeOff should not be called before duration is met", takeOffCalled)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `detect() triggers take-off when take-off conditions are met for required duration`() = runTest {
        // GIVEN: Take-off conditions
        autoStartDetector.velocity = autoStartDetector.velocityFlying + 1.0f
        autoStartDetector.climbrate = autoStartDetector.climbrateTakeOff + 0.5f

        // WHEN: detect() is called for the required 5 seconds
        autoStartDetector.simulateTimePassing(autoStartDetector.takeOffDuration + 200.milliseconds)

        // other wise github action fails
        advanceTimeBy(autoStartDetector.takeOffDuration + 100.milliseconds)

        // THEN: The onTakeOff callback should have been called
        assertTrue("onTakeOff should be called after take-off conditions are met", takeOffCalled)
        assertFalse("onLanded should not be called", landedCalled)
    }

    @Test
    fun `timer resets if take-off conditions are interrupted`() = runTest {
        // GIVEN: Take-off conditions are met for a while
        autoStartDetector.velocity = autoStartDetector.velocityFlying + 1.0f
        autoStartDetector.climbrate = autoStartDetector.climbrateTakeOff + 0.5f
        autoStartDetector.simulateTimePassing(3.seconds)

        // WHEN: The conditions are no longer met
        autoStartDetector.velocity = autoStartDetector.velocityFlying - 1.0f // Below threshold
        autoStartDetector.simulateTimePassing(1.seconds)

        // AND WHEN: The conditions are met again, but not for the full duration
        autoStartDetector.velocity = autoStartDetector.velocityFlying + 1.0f
        autoStartDetector.climbrate = autoStartDetector.climbrateTakeOff + 0.5f
        autoStartDetector.simulateTimePassing(3.seconds) // Total time is 6s, but was interrupted

        // THEN: The flight should not have started because the timer was reset
        assertFalse("onTakeOff should not be called if the condition timer was reset", takeOffCalled)
    }

    @Test
    fun `detect() triggers landed when landing conditions are met for required duration`() = runTest {
        // GIVEN: The detector iterates over all states
        takeOff()
        flying()
        landing()
        landed()

        // THEN: The onTakeOff and onLanded callback should have been called
        assertTrue("onTakeOff should be called after take-off conditions are met", takeOffCalled)
        assertTrue("onLanded should be called after landing conditions are met", landedCalled)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `detect() does not trigger landed if landing is aborted`() = runTest {
        // GIVEN: The detector is already in a flying state
        takeOff()

        // AND GIVEN: Landing conditions are met for a short time
        autoStartDetector.velocity = 0.5f
        autoStartDetector.simulateTimePassing(3.seconds)

        // WHEN: Speed picks up again, aborting the landing
        autoStartDetector.velocity = 2.0f
        autoStartDetector.simulateTimePassing(5.seconds)

        // other wise github action fails
        advanceUntilIdle()

        // THEN: The onLanded callback should not have been called
        assertFalse("onLanded should not be called if landing is aborted", landedCalled)
    }

    @Test
    fun `reset() method resets state and timers`() = runTest {
        // GIVEN: The detector has started a flight
        takeOff()
        assertTrue(takeOffCalled)

        // WHEN: reset() is called
        autoStartDetector.reset()

        // THEN: The detector should be ready to detect a new flight, and timers are zero
        // We can test this by ensuring it can start a new flight from scratch
        takeOffCalled = false // Reset our test flag

        autoStartDetector.velocity = autoStartDetector.velocityFlying + 1.0f
        autoStartDetector.climbrate = autoStartDetector.climbrateTakeOff + 0.5f
        autoStartDetector.simulateTimePassing(autoStartDetector.takeOffDuration + 200.milliseconds)

        assertTrue("Should be able to start a new flight after reset", takeOffCalled)
    }

    @Test
    fun `detect() does nothing when sensor data is not yet initialized`() = runTest {
        // GIVEN: No sensor data set – both remain at default Float.MIN_VALUE
        // (velocity and climbrate are not assigned in this test)

        // WHEN: detect() is called for longer than any detection duration
        autoStartDetector.simulateTimePassing(autoStartDetector.takeOffDuration + 1.seconds)

        // THEN: No callbacks should fire because the guard returns early
        assertFalse("onTakeOff should not fire without initialized sensor data", takeOffCalled)
        assertFalse("onLanded should not fire without initialized sensor data", landedCalled)
    }

    @Test
    fun `Landed state auto-resets to WaitForTakeOff after resetDuration`() = runTest {
        // GIVEN: Full flight and landing cycle
        takeOff()
        flying()
        landing()
        landed()
        assertTrue("onLanded should have been called", landedCalled)

        // WHEN: Landed conditions persist for the full resetDuration (30 s)
        autoStartDetector.velocity = autoStartDetector.velocityLanded - 1f
        autoStartDetector.climbrate = 0f
        autoStartDetector.simulateTimePassing(autoStartDetector.resetDuration + 500.milliseconds)

        // THEN: The state machine should have auto-reset to WaitForTakeOff,
        // so a new take-off cycle must be fully detectable again.
        takeOffCalled = false
        autoStartDetector.velocity = autoStartDetector.velocityFlying + 1f
        autoStartDetector.climbrate = autoStartDetector.climbrateTakeOff + 0.5f
        autoStartDetector.simulateTimePassing(autoStartDetector.takeOffDuration + 200.milliseconds)

        assertTrue("After auto-reset a new take-off should be detected", takeOffCalled)
    }

    @Test
    fun `taxiing after landing does not prevent reset to WaitForTakeOff`() = runTest {
        // GIVEN: Full flight and landing cycle
        takeOff()
        flying()
        landing()
        landed()
        assertTrue("onLanded should have been called", landedCalled)

        // WHEN: Pilot taxis at speed above velocityLanded – the old condition-based
        // accumulator would have reset timeInResetCondition on every tick, making
        // it impossible to ever return to WaitForTakeOff while rolling.
        autoStartDetector.velocity = autoStartDetector.velocityLanded + 5f  // fast taxi / rollout
        autoStartDetector.climbrate = 0f
        autoStartDetector.simulateTimePassing(autoStartDetector.resetDuration + 500.milliseconds)

        // THEN: The wall-clock based reset timer must not care about sensor values.
        // The machine must have returned to WaitForTakeOff and must be able to detect
        // a brand-new take-off.
        takeOffCalled = false
        autoStartDetector.velocity = autoStartDetector.velocityFlying + 1f
        autoStartDetector.climbrate = autoStartDetector.climbrateTakeOff + 0.5f
        autoStartDetector.simulateTimePassing(autoStartDetector.takeOffDuration + 200.milliseconds)

        assertTrue(
            "A new take-off must be detectable after taxiing through the reset period",
            takeOffCalled
        )
    }

    @Test
    fun `landing detection returns to Flying state when flying conditions are restored`() = runTest {
        // GIVEN: Detector is in Flying state
        autoStartDetector.velocity = autoStartDetector.velocityFlying + 1f
        autoStartDetector.climbrate = autoStartDetector.climbrateTakeOff + 0.5f
        autoStartDetector.simulateTimePassing(autoStartDetector.takeOffDuration + 200.milliseconds)
        autoStartDetector.simulateTimePassing(autoStartDetector.flyingDuration + 100.milliseconds)

        // AND: Landing conditions begin but do NOT last the full landingDuration
        autoStartDetector.climbrate = autoStartDetector.climbrateLanding - 0.5f
        autoStartDetector.simulateTimePassing(autoStartDetector.landingDuration - 1.seconds)

        // WHEN: Flying conditions are restored for the full flyingDuration
        // → triggers the Landing → Flying back-transition
        autoStartDetector.climbrate = 0f // neutral climbrate, speed stays above threshold
        autoStartDetector.simulateTimePassing(autoStartDetector.flyingDuration + 100.milliseconds)

        // THEN: No landing should have been detected
        assertFalse("onLanded should not be called when landing was aborted mid-state", landedCalled)

        // AND: A full landing cycle is still possible from this point
        autoStartDetector.climbrate = autoStartDetector.climbrateLanding - 0.5f
        autoStartDetector.simulateTimePassing(autoStartDetector.landingDuration + 100.milliseconds)
        autoStartDetector.velocity = autoStartDetector.velocityLanded - 1f
        autoStartDetector.climbrate = 0f
        autoStartDetector.simulateTimePassing(autoStartDetector.landedDuration + 500.milliseconds)

        assertTrue("onLanded should be called after the full subsequent landing", landedCalled)
    }

    // -------------------------------------------------------------------------
    // 🟡 Wichtig: Korrektheitsnachweise
    // -------------------------------------------------------------------------

    @Test
    fun `onTakeOff is called exactly once regardless of how long conditions persist`() = runTest {
        // GIVEN: Track the exact invocation count
        var takeOffCount = 0
        autoStartDetector.onTakeOff = { takeOffCount++ }

        // WHEN: Take-off conditions persist for three times the required duration
        autoStartDetector.velocity = autoStartDetector.velocityFlying + 1f
        autoStartDetector.climbrate = autoStartDetector.climbrateTakeOff + 0.5f
        autoStartDetector.simulateTimePassing(autoStartDetector.takeOffDuration * 3)

        // THEN: Callback fired exactly once (no repeated TakeOff side effect)
        assertEquals("onTakeOff must be called exactly once", 1, takeOffCount)
    }

    @Test
    fun `onLanded is called exactly once regardless of how long conditions persist`() = runTest {
        // GIVEN: Track the exact invocation count
        var landedCount = 0
        autoStartDetector.onLanded = { landedCount++ }

        // WHEN: A complete flight with an extended landed phase
        autoStartDetector.velocity = autoStartDetector.velocityFlying + 1f
        autoStartDetector.climbrate = autoStartDetector.climbrateTakeOff + 0.5f
        autoStartDetector.simulateTimePassing(autoStartDetector.takeOffDuration + 200.milliseconds)
        autoStartDetector.simulateTimePassing(autoStartDetector.flyingDuration + 100.milliseconds)
        autoStartDetector.climbrate = autoStartDetector.climbrateLanding - 0.5f
        autoStartDetector.simulateTimePassing(autoStartDetector.landingDuration + 100.milliseconds)
        autoStartDetector.velocity = autoStartDetector.velocityLanded - 1f
        autoStartDetector.climbrate = 0f
        autoStartDetector.simulateTimePassing(autoStartDetector.landedDuration * 3)

        // THEN: Callback fired exactly once
        assertEquals("onLanded must be called exactly once", 1, landedCount)
    }

    @Test
    fun `take-off requires both velocity AND climbrate conditions simultaneously`() = runTest {
        // Case 1: only velocity above threshold, climbrate below climbrateTakeOff
        autoStartDetector.velocity = autoStartDetector.velocityFlying + 1f
        autoStartDetector.climbrate = autoStartDetector.climbrateTakeOff - 0.1f
        autoStartDetector.simulateTimePassing(autoStartDetector.takeOffDuration + 1.seconds)
        assertFalse("onTakeOff must not fire when only velocity condition is met", takeOffCalled)

        // Case 2: only climbrate above threshold, velocity below velocityFlying
        autoStartDetector.velocity = autoStartDetector.velocityFlying - 0.1f
        autoStartDetector.climbrate = autoStartDetector.climbrateTakeOff + 0.5f
        autoStartDetector.simulateTimePassing(autoStartDetector.takeOffDuration + 1.seconds)
        assertFalse("onTakeOff must not fire when only climbrate condition is met", takeOffCalled)
    }

    @Test
    fun `take-off triggers when velocity is exactly at the threshold boundary`() = runTest {
        // GIVEN: velocity == velocityFlying (boundary – the condition uses >=)
        autoStartDetector.velocity = autoStartDetector.velocityFlying
        autoStartDetector.climbrate = autoStartDetector.climbrateTakeOff + 0.5f

        // WHEN
        autoStartDetector.simulateTimePassing(autoStartDetector.takeOffDuration + 200.milliseconds)

        // THEN: >= is inclusive, so exactly-at-threshold must trigger
        assertTrue("onTakeOff should fire when velocity is exactly at the threshold", takeOffCalled)
    }

    @Test
    fun `Flying transitions directly to Landed when velocity drops to zero`() = runTest {
        // GIVEN: The detector is in Flying state via manual start
        autoStartDetector.velocity = autoStartDetector.velocityFlying + 1f
        autoStartDetector.climbrate = autoStartDetector.climbrateTakeOff + 0.5f
        autoStartDetector.simulateTimePassing(autoStartDetector.takeOffDuration + 200.milliseconds)
        autoStartDetector.simulateTimePassing(autoStartDetector.flyingDuration + 100.milliseconds)
        assertTrue("onTakeOff should have fired", takeOffCalled)

        // WHEN: Velocity drops to 0 and climbrate is near-zero (simulator scenario)
        // without passing through a classic landing approach (fast + sinking)
        autoStartDetector.velocity = 0f
        autoStartDetector.climbrate = 0.004f
        autoStartDetector.simulateTimePassing(autoStartDetector.landedDuration + 500.milliseconds)

        // THEN: The detector should transition directly Flying → Landed
        assertTrue("onLanded should fire via direct Flying→Landed path", landedCalled)
    }

    @Test
    fun `Flying does not transition to Landed before landedDuration elapsed`() = runTest {
        // GIVEN: In flying state
        autoStartDetector.velocity = autoStartDetector.velocityFlying + 1f
        autoStartDetector.climbrate = autoStartDetector.climbrateTakeOff + 0.5f
        autoStartDetector.simulateTimePassing(autoStartDetector.takeOffDuration + 200.milliseconds)
        autoStartDetector.simulateTimePassing(autoStartDetector.flyingDuration + 100.milliseconds)

        // WHEN: Landed conditions but NOT for the full duration
        autoStartDetector.velocity = 0f
        autoStartDetector.climbrate = 0f
        autoStartDetector.simulateTimePassing(autoStartDetector.landedDuration - 1.seconds)

        // THEN: Should still be in Flying (not yet landed)
        assertFalse("onLanded should not fire before landedDuration", landedCalled)
    }

    // -------------------------------------------------------------------------
    // 🟢 Robustheit: neue var-Features aus dem Fix
    // -------------------------------------------------------------------------

    @Test
    fun `changing velocityFlying threshold at runtime immediately affects detection`() = runTest {
        // GIVEN: Velocity above the original threshold
        val originalThreshold = autoStartDetector.velocityFlying
        autoStartDetector.velocity = originalThreshold + 1f
        autoStartDetector.climbrate = autoStartDetector.climbrateTakeOff + 0.5f

        // WHEN: Threshold is raised above current velocity mid-flight
        autoStartDetector.velocityFlying = autoStartDetector.velocity + 1f
        autoStartDetector.simulateTimePassing(autoStartDetector.takeOffDuration + 1.seconds)

        // THEN: No take-off (velocity now below the new threshold)
        assertFalse("onTakeOff must not fire when velocity is below the updated threshold", takeOffCalled)

        // WHEN: Threshold is restored below the current velocity
        autoStartDetector.velocityFlying = originalThreshold
        autoStartDetector.simulateTimePassing(autoStartDetector.takeOffDuration + 200.milliseconds)

        // THEN: Take-off now detectable with the restored threshold
        assertTrue("onTakeOff should fire after threshold is restored below velocity", takeOffCalled)
    }

    @Test
    fun `default empty callbacks do not throw when triggered without explicit assignment`() = runTest {
        // GIVEN: A fresh detector with no callbacks assigned (uses default = {})
        val freshDetector = AutoStartDetector(timeProvider = { testTimeMillis })

        // WHEN: A full take-off cycle runs through (SideEffect.TakeOff fires)
        freshDetector.velocity = freshDetector.velocityFlying + 1f
        freshDetector.climbrate = freshDetector.climbrateTakeOff + 0.5f

        // THEN: No UninitializedPropertyAccessException – reaching here means success
        freshDetector.simulateTimePassing(freshDetector.takeOffDuration + 200.milliseconds)
    }

    /**
     * Helper function to force the state machine into a flying state for testing landing logic.
     */
    private fun takeOff() {
        autoStartDetector.velocity = autoStartDetector.velocityFlying + 1f
        autoStartDetector.climbrate = autoStartDetector.climbrateTakeOff + 0.5f

        // Reset the flag so we can test for the stop event specifically
        takeOffCalled = false
        landedCalled = false

        autoStartDetector.simulateTimePassing(autoStartDetector.takeOffDuration + 200.milliseconds)
    }

    private fun flying() {
        autoStartDetector.velocity = autoStartDetector.velocityFlying + 1f
        autoStartDetector.simulateTimePassing(autoStartDetector.flyingDuration + 100.milliseconds)
    }

    private fun landing() {
        autoStartDetector.velocity = autoStartDetector.velocityFlying + 1f
        autoStartDetector.climbrate = autoStartDetector.climbrateLanding - 0.5f
        autoStartDetector.simulateTimePassing(autoStartDetector.landingDuration + 100.milliseconds)
    }

    private fun landed() {
        autoStartDetector.velocity = autoStartDetector.velocityLanded - 1f
        autoStartDetector.climbrate = 0f
        autoStartDetector.simulateTimePassing(autoStartDetector.landedDuration + 500.milliseconds)
    }

    /**
     * Helper extension function to simulate time passing by calling detect() in a loop.
     * This makes the tests much cleaner and more readable.
     */
    private fun AutoStartDetector.simulateTimePassing(duration: Duration, tickIntervalMs: Long = 100) {
        var elapsed = 0L
        val totalMs = duration.inWholeMilliseconds
        while (elapsed < totalMs) {
            // advance our fake clock
            testTimeMillis += tickIntervalMs
            // call detect so the detector uses the advanced time
            this.detect()
            elapsed += tickIntervalMs
        }
    }
}
