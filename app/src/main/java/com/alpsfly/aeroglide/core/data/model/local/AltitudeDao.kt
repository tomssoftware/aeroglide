package com.alpsfly.aeroglide.core.data.model.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.alpsfly.aeroglide.core.data.model.local.Altitude

@Dao
interface AltitudeDao {
    @get:Query("select altitude_timestamp As timestamp, altitude from track_log")
    val allAltitudes: LiveData<List<Altitude>>

    @Query("select altitude_timestamp As timestamp, altitude from track_log where climbrate_timestamp >= :datetime")
    fun getAltitudesSince(datetime: Long): List<Altitude>

    @Query("select altitude_timestamp As timestamp, altitude from track_log where track_id == :trackId")
    fun getTrackAltitudes(trackId: Long): List<Altitude>

    @Query("select altitude_timestamp As timestamp, altitude from track_log where track_id == :trackId and climbrate_timestamp >= :datetime")
    fun getTrackAltitudesSince(trackId: Long, datetime: Long): List<Altitude>
}