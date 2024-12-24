package com.alpsfly.aeroglide.core.data.model.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface HeartrateDao {
    @get:Query("select heartrate_timestamp As timestamp, heartrate from track_log")
    val allHeartrates: LiveData<List<Heartrate>>

    @Query("select heartrate_timestamp As timestamp, heartrate from track_log where heartrate_timestamp >= :datetime")
    fun getHeartratesSince(datetime: Long): List<Heartrate>

    @Query("select heartrate_timestamp As timestamp, heartrate from track_log where track_id == :trackId")
    fun getTrackHeartrates(trackId: Long): List<Heartrate>

    @Query("select heartrate_timestamp As timestamp, heartrate from track_log where track_id == :trackId and heartrate_timestamp >= :datetime")
    fun getTrackHeartratesSince(trackId: Long, datetime: Long): List<Heartrate>
}