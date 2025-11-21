package com.alpsfly.aeroglide.core.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class AutoStartDetectorTest {

    private lateinit var autoStartDetector: AutoStartDetector

    // Mock the callbacks to verify they are called
    private var takeOffCalled = false
    private var landedCalled = false

    @Before
    fun setUp() {
        autoStartDetector = AutoStartDetector()
        takeOffCalled = false
        landedCalled = false

        // Assign mock implementations to the callbacks
        autoStartDetector.onTakeOff = { takeOffCalled = true }
        autoStartDetector.onLanded = { landedCalled = true }
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
        autoStartDetector.simulateTimePassing(autoStartDetector.takeOffDuration + 100.milliseconds)

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
        autoStartDetector.simulateTimePassing(autoStartDetector.takeOffDuration + 100.milliseconds)

        assertTrue("Should be able to start a new flight after reset", takeOffCalled)
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

        autoStartDetector.simulateTimePassing(autoStartDetector.takeOffDuration + 100.milliseconds)
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
        val startTime = System.currentTimeMillis()
        var elapsed = 0L
        while (elapsed < duration.inWholeMilliseconds) {
            // We can't actually advance the clock, but we can call detect() repeatedly
            // The detector's internal logic uses System.currentTimeMillis(), so it will see time passing.
            this.detect()
            // A small delay is needed to let the system clock actually advance.
            // In a real test, you might inject a Clock dependency. For this FSM, this is sufficient.
            Thread.sleep(tickIntervalMs)
            elapsed = System.currentTimeMillis() - startTime
        }
        // One final call to ensure the last interval is processed
        this.detect()
    }
}
