package com.alpsfly.aeroglide.core.data.database

import android.content.Context
import androidx.lifecycle.LiveData
import com.alpsfly.aeroglide.AeroGlideApplication
import com.alpsfly.aeroglide.AeroGlideDatabase
import com.alpsfly.aeroglide.core.data.model.local.Altitude
import com.alpsfly.aeroglide.core.data.model.local.Climbrate
import com.alpsfly.aeroglide.core.data.model.local.Track
import com.alpsfly.aeroglide.core.data.model.local.TrackLog
import com.alpsfly.aeroglide.core.data.model.local.User
import com.alpsfly.aeroglide.core.data.model.local.Velocity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface ILocalDataSource {
    val allTracks: LiveData<MutableList<Track>>

    suspend fun getTrack(trackId: Long): List<Track>

    suspend fun getClimbratesSince(datetime: Long): List<Climbrate>

    suspend fun getTrackClimbrates(trackId: Long): List<Climbrate>

    suspend fun getTrackLog(trackId: Long): List<TrackLog>

    suspend fun getAvgSpeed(trackId: Long): Float

    suspend fun getMinAltitude(trackId: Long): Float

    suspend fun getTrackAltitudes(trackId: Long): List<Altitude>

    suspend fun getTrackVelocities(trackId: Long): List<Velocity>

    suspend fun insertTrack(track: Track)

    suspend fun updateTrack(track: Track)

    suspend fun deleteTrack(track: Track)

    suspend fun insertTrackLog(trackLog: TrackLog)

    suspend fun getUser(userId: String): List<User>
}

@Singleton
class LocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val application: AeroGlideApplication,
    private val database: AeroGlideDatabase
) : ILocalDataSource {
    override val allTracks: LiveData<MutableList<Track>> = database.trackDao().allTracks

    override suspend fun getTrack(trackId: Long) = withContext(Dispatchers.IO) {
        database.trackDao().getTrack(trackId)
    }
    override suspend fun getClimbratesSince(datetime: Long) = withContext(Dispatchers.IO) {
        database.climbrateDao().getClimbratesSince(datetime)
    }

    override suspend fun getTrackClimbrates(trackId: Long) = withContext(Dispatchers.IO) {
        database.climbrateDao().getTrackClimbrates(trackId)
    }

    override suspend fun getTrackLog(trackId: Long) = withContext(Dispatchers.IO) {
        database.trackLogDao().getTrackLog(trackId)
    }

    override suspend fun getAvgSpeed(trackId: Long) = withContext(Dispatchers.IO) {
        database.trackLogDao().getAvgSpeed(trackId) // todo: extend Track and calculate while recording
    }

    override suspend fun getMinAltitude(trackId: Long) = withContext(Dispatchers.IO) {
        database.trackLogDao().getMinAltitude(trackId) // todo: extend Track calculate while recording
    }

    override suspend fun getTrackAltitudes(trackId: Long) = withContext(Dispatchers.IO) {
        database.altitudeDao().getTrackAltitudes(trackId)
    }

    override suspend fun getTrackVelocities(trackId: Long) = withContext(Dispatchers.IO) {
        database.velocityDao().getTrackVelocities(trackId)
    }

    override suspend fun insertTrack(track: Track) = withContext(Dispatchers.IO) {
        if (hasExpectedDeviceContext()) {
            check(track.trackId != 0L) { "Invalid TrackId" }
            database.trackDao().insertTrack(track)
        }
    }

    override suspend fun updateTrack(track: Track) = withContext(Dispatchers.IO) {
        if (hasExpectedDeviceContext()) {
            check(track.trackId != 0L) { "Invalid TrackId" }
            database.trackDao().updateTrack(track)
        }
    }

    override suspend fun deleteTrack(track: Track) = withContext(Dispatchers.IO) {
        if (hasExpectedDeviceContext()) {
            check(track.trackId != 0L) { "Invalid TrackId" }
            database.trackDao().deleteTrack(track.trackId)
        }
    }

    override suspend fun insertTrackLog(trackLog: TrackLog) = withContext(Dispatchers.IO) {
        if (hasExpectedDeviceContext()) {
            check(trackLog.trackLogId != 0L)
            check(trackLog.trackId != 0L) { "Invalid TrackId" }
            check(trackLog.altitudeTimestamp != 0L) { "Invalid timestamp" }
            check(trackLog.climbrateTimestamp != 0L) { "Invalid timestamp" }
            database.trackLogDao().addTrackLog(trackLog)
        }
    }

    override suspend fun getUser(userId: String) = withContext(Dispatchers.IO) {
        database.userDao().getUser(userId)
    }

    private fun hasExpectedDeviceContext() = true

//    companion object {
//        @Volatile
//        private var INSTANCE: LocalDataSource? = null
//
//        fun getInstance(application: AeroGlideApplication, database: AeroGlideDatabase): LocalDataSource =
//            INSTANCE ?: synchronized(this) {
//                INSTANCE ?: LocalDataSource(application, database).also {
//                    INSTANCE = it
//                }
//            }
//    }
}