package com.thermalscout.database

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Created by Thomas on 25.02.2018.
 */

@Dao
interface TrackDao {

    @get:Query("select * from track order by departure_time DESC")
    val allTracks: LiveData<MutableList<Track>>

    @Query("select * from track where track_id = :id")
    fun theTrack(id: Long): LiveData<List<Track>>

    @Query("select track_id from track")
    fun getAllTrackIds(): List<Long>

    @Query("select * from track where track_id = :id")
    fun getTrack(id: Long): List<Track>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrack(track: Track)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateTrack(track: Track)

    @Query("delete from track")
    fun deleteAllTracks()

    @Query("delete from track where track_id = :trackId")
    fun deleteTrack(trackId: Long)
}
