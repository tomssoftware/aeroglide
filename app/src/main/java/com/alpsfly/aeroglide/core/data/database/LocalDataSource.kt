package com.alpsfly.aeroglide.core.data.database

import androidx.lifecycle.LiveData
import com.alpsfly.aeroglide.AeroGlideApplication
import com.alpsfly.aeroglide.AeroGlideDatabase
import com.alpsfly.aeroglide.core.data.model.local.Track
import com.alpsfly.aeroglide.core.data.model.local.TrackLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalDataSource private constructor(
    private val application: AeroGlideApplication,
    private val database: AeroGlideDatabase
) {
    val allTracks: LiveData<MutableList<Track>> = database.trackDao().allTracks

    suspend fun getTrack(trackId: Long) = withContext(Dispatchers.IO) {
        database.trackDao().getTrack(trackId)
    }
    suspend fun getClimbratesSince(datetime: Long) = withContext(Dispatchers.IO) {
        database.climbrateDao().getClimbratesSince(datetime)
    }

    suspend fun getTrackClimbrates(trackId: Long) = withContext(Dispatchers.IO) {
        database.climbrateDao().getTrackClimbrates(trackId)
    }

    suspend fun getTrackLog(trackId: Long) = withContext(Dispatchers.IO) {
        database.trackLogDao().getTrackLog(trackId)
    }

    suspend fun getAvgSpeed(trackId: Long) = withContext(Dispatchers.IO) {
        database.trackLogDao().getAvgSpeed(trackId) // todo: extend Track and calculate while recording
    }

    suspend fun getMinAltitude(trackId: Long) = withContext(Dispatchers.IO) {
        database.trackLogDao().getMinAltitude(trackId) // todo: extend Track calculate while recording
    }

    suspend fun getTrackAltitudes(trackId: Long) = withContext(Dispatchers.IO) {
        database.altitudeDao().getTrackAltitudes(trackId)
    }

    suspend fun getTrackVelocities(trackId: Long) = withContext(Dispatchers.IO) {
        database.velocityDao().getTrackVelocities(trackId)
    }

    suspend fun insertTrack(track: Track) = withContext(Dispatchers.IO) {
        if (hasExpectedDeviceContext()) {
            check(track.trackId != 0L) { "Invalid TrackId" }
            database.trackDao().insertTrack(track)
        }
    }

    suspend fun updateTrack(track: Track) = withContext(Dispatchers.IO) {
        if (hasExpectedDeviceContext()) {
            check(track.trackId != 0L) { "Invalid TrackId" }
            database.trackDao().updateTrack(track)
        }
    }

    suspend fun deleteTrack(track: Track) = withContext(Dispatchers.IO) {
        if (hasExpectedDeviceContext()) {
            check(track.trackId != 0L) { "Invalid TrackId" }
            database.trackDao().deleteTrack(track.trackId)
        }
    }

    suspend fun insertTrackLog(trackLog: TrackLog) = withContext(Dispatchers.IO) {
        if (hasExpectedDeviceContext()) {
            check(trackLog.trackLogId != 0L)
            check(trackLog.trackId != 0L) { "Invalid TrackId" }
            check(trackLog.altitudeTimestamp != 0L) { "Invalid timestamp" }
            check(trackLog.climbrateTimestamp != 0L) { "Invalid timestamp" }
            database.trackLogDao().addTrackLog(trackLog)
        }
    }

    suspend fun getUser(userId: String) = withContext(Dispatchers.IO) {
        database.userDao().getUser(userId)
    }

    private fun hasExpectedDeviceContext() = true

    companion object {
        @Volatile
        private var INSTANCE: LocalDataSource? = null

        fun getInstance(application: AeroGlideApplication, database: AeroGlideDatabase): LocalDataSource =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocalDataSource(application, database).also {
                    INSTANCE = it
                }
            }
    }
}