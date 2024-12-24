package com.alpsfly.aeroglide.core.data.model.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface PositionDao {
    @get:Query("select position_timestamp As timestamp, latitude, longitude, accuracy from track_log")
    val allPositions: LiveData<List<Position>>

    @Query("select position_timestamp As timestamp, latitude, longitude, accuracy from track_log where track_log_id = :trackLogId")
    fun getPosition(trackLogId: Long): LiveData<List<Position>>

    @Query("select position_timestamp As timestamp, latitude, longitude, accuracy from track_log where track_id = :trackId")
    fun getTrackPositions(trackId: Long): List<Position>

    @Query("select position_timestamp As timestamp, latitude, longitude, accuracy from track_log where track_log_id >= :datetime")
    fun getPositionsSince(datetime: Long): List<Position>
}