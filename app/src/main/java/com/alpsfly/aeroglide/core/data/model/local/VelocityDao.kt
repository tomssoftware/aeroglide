package com.alpsfly.aeroglide.core.data.model.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface VelocityDao {
    @get:Query("select velocity_timestamp As timestamp, velocity from track_log")
    val allVelocities: LiveData<List<Velocity>>

    @Query("select velocity_timestamp As timestamp, velocity from track_log where velocity_timestamp >= :datetime")
    fun getVelocitiesSince(datetime: Long): List<Velocity>

    @Query("select velocity_timestamp As timestamp, velocity from track_log where track_id == :trackId")
    fun getTrackVelocities(trackId: Long): List<Velocity>

    @Query("select velocity_timestamp As timestamp, velocity from track_log where track_id == :trackId and velocity_timestamp >= :datetime")
    fun getTrackVelocitiesSince(trackId: Long, datetime: Long): List<Velocity>
}