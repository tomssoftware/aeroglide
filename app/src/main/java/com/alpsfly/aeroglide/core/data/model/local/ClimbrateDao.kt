package com.thermalscout.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface ClimbrateDao {
    @get:Query("select climbrate_timestamp As timestamp, climbrate, grade from track_log")
    val allClimbrates: LiveData<List<Climbrate>>

    @Query("select climbrate_timestamp As timestamp, climbrate, grade from track_log where climbrate_timestamp >= :datetime")
    fun getClimbratesSince(datetime: Long): List<Climbrate>

    @Query("select climbrate_timestamp As timestamp, climbrate, grade from track_log where track_id == :trackId")
    fun getTrackClimbrates(trackId: Long): List<Climbrate>

    @Query("select climbrate_timestamp As timestamp, climbrate, grade from track_log where track_id == :trackId and climbrate_timestamp >= :datetime")
    fun getTrackClimbratesSince(trackId: Long, datetime: Long): List<Climbrate>

    @Query("select climbrate_timestamp As timestamp, climbrate, grade from track_log where track_id == :trackId")
    fun getTrackGrades(trackId: Long): List<Climbrate>
}