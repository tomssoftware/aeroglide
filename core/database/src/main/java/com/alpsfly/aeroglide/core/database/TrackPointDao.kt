package com.alpsfly.aeroglide.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alpsfly.aeroglide.core.model.database.TrackPoint
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackPointDao {
    @get:Query("SELECT * FROM track_point ORDER BY timestamp")
    val allTrackPoints: Flow<List<TrackPoint>>

    @Query("SELECT * FROM track_point WHERE timestamp = :timestamp")
    fun getTrackPoint(timestamp: Long): Flow<TrackPoint?>

    @Query("SELECT * FROM track_point WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp")
    fun getTrackPointsBetween(start: Long, end: Long): Flow<List<TrackPoint>>

    @Query("SELECT * FROM track_point WHERE sync_state = 'PENDING'")
    fun getPendingTrackPoints(): List<TrackPoint>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTrackPoint(trackPoint: TrackPoint)

    @Query("DELETE FROM track_point")
    suspend fun deleteAllTrackPoints()
}
