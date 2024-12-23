package com.thermalscout.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query

@Dao
interface TrackLogDao {

    @get:Query("SELECT * from track_log")
    val allTrackLogs: LiveData<List<TrackLog>>

    @get:Query("SELECT * from track_log")
    val trackLogList: List<TrackLog>

    @Query("select * from track_log where track_id = :trackId order by track_log_id")
    fun getTrackLog(trackId: Long): List<TrackLog>

    @Insert(onConflict = REPLACE)
    fun addTrackLog(trackLog: TrackLog)

    @Query("DELETE from track_log")
    fun deleteAll()

    @Query("DELETE from track_log where track_id = :trackId")
    fun deleteTrack(trackId: Long)

    @Query("select avg(velocity) from track_log where track_id = :trackId")
    fun getAvgSpeed(trackId: Long): Float

    @Query("select min(altitude) from track_log where track_id = :trackId")
    fun getMinAltitude(trackId: Long): Float
}