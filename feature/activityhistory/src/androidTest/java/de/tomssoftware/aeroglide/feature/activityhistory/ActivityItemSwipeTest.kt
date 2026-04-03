package de.tomssoftware.aeroglide.feature.activityhistory

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.tomssoftware.aeroglide.core.model.database.Activity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityItemSwipeTest {

    @get:Rule
    val composeRule = createComposeRule()

    /** Helper function: Creates a test activity with the given ID */
    private fun testActivity(id: Long) = Activity(
        activityId = id,
        begin = 1_700_000_000_000L,
        end   = 1_700_003_600_000L,
        distance = 10_000f
    )

    /**
     * Test 1: Exactly one item is deleted after swiping right.
     */
    @Test
    fun swipeRight_deletesExactlyOneItem() {
        val activities = (1L..4L).map { testActivity(it) }
        val deletedItems = mutableListOf<Activity>()

        composeRule.setContent {
            val list = remember { mutableStateListOf(*activities.toTypedArray()) }
            val navController = rememberNavController()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(
                    items = list,
                    key = { it.activityId }
                ) { activity ->
                    ActivityItem(
                        activity = activity,
                        navController = navController,
                        onRemove = { removed ->
                            deletedItems.add(removed)
                            list.remove(removed)
                        }
                    )
                }
            }
        }

        composeRule.waitForIdle()

        // Swipe the first item fully to the right
        composeRule.onAllNodes(
            androidx.compose.ui.test.hasClickAction()
        )[0].performTouchInput { swipeRight() }

        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(500)

        assertEquals("Exactly 1 item must be deleted", 1, deletedItems.size)
    }

    /**
     * Test 2: Swiping halfway (below the 50% threshold) deletes nothing.
     */
    @Test
    fun swipeHalfway_doesNotDeleteItem() {
        val activities = (1L..3L).map { testActivity(it) }
        val deletedItems = mutableListOf<Activity>()

        composeRule.setContent {
            val list = remember { mutableStateListOf(*activities.toTypedArray()) }
            val navController = rememberNavController()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(
                    items = list,
                    key = { it.activityId }
                ) { activity ->
                    ActivityItem(
                        activity = activity,
                        navController = navController,
                        onRemove = { removed ->
                            deletedItems.add(removed)
                            list.remove(removed)
                        }
                    )
                }
            }
        }

        composeRule.waitForIdle()

        // Swipe from left edge to 30% of the width (clearly below the 50% threshold)
        composeRule.onAllNodes(
            androidx.compose.ui.test.hasClickAction()
        )[0].performTouchInput {
            val startX = left
            val endX = left + (right - left) * 0.30f
            // Duration ensures we don't trigger high-velocity dismissal
            swipeRight(startX = startX, endX = endX, durationMillis = 400)
        }

        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(500)

        assertEquals("No item should be deleted when swiping 30%", 0, deletedItems.size)
    }

    /**
     * Test 3: Multiple deletions – each time exactly 1 item is removed.
     */
    @Test
    fun swipeMultipleTimes_eachTimeOnlyOneItemDeleted() {
        val activities = (1L..4L).map { testActivity(it) }
        val deletedIds = mutableListOf<Long>()

        composeRule.setContent {
            val list = remember { mutableStateListOf(*activities.toTypedArray()) }
            val navController = rememberNavController()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(
                    items = list,
                    key = { it.activityId }
                ) { activity ->
                    ActivityItem(
                        activity = activity,
                        navController = navController,
                        onRemove = { removed ->
                            deletedIds.add(removed.activityId)
                            list.remove(removed)
                        }
                    )
                }
            }
        }

        repeat(3) { swipeCount ->
            composeRule.waitForIdle()
            val countBefore = deletedIds.size

            composeRule.onAllNodes(
                androidx.compose.ui.test.hasClickAction()
            )[0].performTouchInput { swipeRight() }

            composeRule.waitForIdle()
            composeRule.mainClock.advanceTimeBy(500)

            val deletedThisRound = deletedIds.size - countBefore
            assertEquals(
                "Swipe #${swipeCount + 1}: Expected 1 deletion, got $deletedThisRound",
                1,
                deletedThisRound
            )
        }
    }

    /**
     * Test 4: After deleting an item, the remaining items are preserved.
     */
    @Test
    fun afterDelete_remainingItemsAreUnchanged() {
        val activities = (1L..3L).map { testActivity(it) }

        composeRule.setContent {
            val list = remember { mutableStateListOf(*activities.toTypedArray()) }
            val navController = rememberNavController()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(
                    items = list,
                    key = { it.activityId }
                ) { activity ->
                    ActivityItem(
                        activity = activity,
                        navController = navController,
                        onRemove = { list.remove(it) }
                    )
                }
            }
        }

        composeRule.waitForIdle()

        composeRule.onAllNodes(
            androidx.compose.ui.test.hasClickAction()
        )[0].performTouchInput { swipeRight() }

        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(500)

        assertFalse(
            "Remaining items should still be present",
            composeRule.onAllNodes(androidx.compose.ui.test.hasClickAction()).fetchSemanticsNodes().isEmpty()
        )
    }
}
