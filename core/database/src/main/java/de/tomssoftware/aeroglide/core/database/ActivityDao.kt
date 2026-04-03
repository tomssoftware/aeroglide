package de.tomssoftware.aeroglide.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import de.tomssoftware.aeroglide.core.model.database.Activity
import de.tomssoftware.aeroglide.core.model.database.SyncState
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("select * from activity")
    fun allActivitiesFlow(): Flow<List<Activity>>

    @Query("SELECT * FROM activity WHERE activity_id = :activityId")
    fun getActivityFlow(activityId: Long): Flow<Activity?>

    @Query("SELECT * FROM activity WHERE activity_id = :activityId")
    suspend fun getActivity(activityId: Long): Activity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addActivity(activity: Activity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateActivity(activity: Activity)

    @Delete
    suspend fun deleteActivity(activity: Activity)

    // ------------------------------------------------------------------
    // Sync-related queries
    // ------------------------------------------------------------------

    /** Returns all activities whose [SyncState] matches [state]. */
    @Query("SELECT * FROM activity WHERE sync_state = :state")
    suspend fun getActivitiesBySyncState(state: SyncState): List<Activity>

    /**
     * Sets [state], [firestoreId] and [timestamp] for a given activity.
     * Also clears any previous sync error.
     * Use for SYNCED and PENDING_UPLOAD transitions.
     */
    @Query(
        "UPDATE activity " +
        "SET sync_state = :state, firestore_id = :firestoreId, last_synced_at = :timestamp, sync_error = NULL " +
        "WHERE activity_id = :id"
    )
    suspend fun updateSyncState(id: Long, state: SyncState, firestoreId: String?, timestamp: Long)

    /**
     * Sets only the [state] without touching the Firestore ID or sync timestamp.
     * Use for PENDING_UPLOAD to avoid overwriting a previously stored Firestore ID.
     */
    @Query("UPDATE activity SET sync_state = :state WHERE activity_id = :id")
    suspend fun updateSyncStateOnly(id: Long, state: SyncState)

    /**
     * Marks an activity as [SyncState.ERROR] and stores a human-readable [error] message.
     */
    @Query("UPDATE activity SET sync_state = :state, sync_error = :error WHERE activity_id = :id")
    suspend fun setSyncError(id: Long, state: SyncState, error: String?)
}
