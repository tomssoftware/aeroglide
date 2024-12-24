package com.alpsfly.aeroglide

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.alpsfly.aeroglide.core.data.database.LocalDataSource
import com.alpsfly.aeroglide.core.data.model.local.Track
import com.alpsfly.aeroglide.core.data.model.local.TrackLog
import com.alpsfly.aeroglide.core.data.model.network.Pilot
import com.alpsfly.aeroglide.core.data.model.network.firebase.Purchase
import com.alpsfly.aeroglide.core.data.model.network.firebase.User
import com.alpsfly.aeroglide.core.data.model.network.mapbox.Direction
import com.alpsfly.aeroglide.core.data.network.WebDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class AeroGlideRepository private constructor(
    private val localDataSource: LocalDataSource,
    private val webDataSource: WebDataSource
) {
    private val auth = Firebase.auth

    val currentUser = callbackFlow {
        val firebaseAuthStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
            if (auth.currentUser != null) {
            }
        }
        auth.addAuthStateListener(firebaseAuthStateListener)
        awaitClose {
            auth.removeAuthStateListener(firebaseAuthStateListener)
        }
    }

    val allTracks: LiveData<MutableList<Track>> = localDataSource.allTracks

    suspend fun getTrack(trackId: Long) = withContext(Dispatchers.IO) {
        localDataSource.getTrack(trackId)
    }

    suspend fun getTrackClimbrates(trackId: Long) = withContext(Dispatchers.IO) {
        localDataSource.getTrackClimbrates(trackId)
    }

    suspend fun getTrackLog(trackId: Long) = withContext(Dispatchers.IO) {
        localDataSource.getTrackLog(trackId)
    }

    suspend fun getAvgSpeed(trackId: Long) = withContext(Dispatchers.IO) {
        localDataSource.getAvgSpeed(trackId) // todo: extend Track and calculate while recording
    }

    suspend fun getMinAltitude(trackId: Long) = withContext(Dispatchers.IO) {
        localDataSource.getMinAltitude(trackId) // todo: extend Track calculate while recording
    }

    suspend fun getTrackAltitudes(trackId: Long) = withContext(Dispatchers.IO) {
        localDataSource.getTrackAltitudes(trackId)
    }

    suspend fun getTrackVelocities(trackId: Long) = withContext(Dispatchers.IO) {
        localDataSource.getTrackVelocities(trackId)
    }

    suspend fun insertTrack(track: Track) = withContext(Dispatchers.IO) {
        localDataSource.insertTrack(track)
    }

    suspend fun updateTrack(track: Track) = withContext(Dispatchers.IO) {
        localDataSource.updateTrack(track)
    }

    suspend fun deleteTrack(track: Track) = withContext(Dispatchers.IO) {
        localDataSource.deleteTrack(track)
    }

    suspend fun insertTrackLog(trackLog: TrackLog) = withContext(Dispatchers.IO) {
        localDataSource.insertTrackLog(trackLog)
    }

    suspend fun getUser(userId: String) = withContext(Dispatchers.IO) {
        localDataSource.getUser(userId)
    }

    suspend fun loadRoute(origin: Location, destination: Location) = withContext(Dispatchers.IO) {
        webDataSource.loadRoute(origin, destination)
    }

    private var _route = MutableLiveData(Direction())
    val route: LiveData<Direction>
        get() = _route

    suspend fun setRoute(route: Direction) = withContext(Dispatchers.Main) {
        _route.value = route
    }

    suspend fun readBlacklists() = webDataSource.readBlacklists()

    suspend fun writeUser(userId: String, data: User) = webDataSource.writeUser(userId, data)

    suspend fun writePilot(userId: String, pilot: Pilot) = webDataSource.writePilot(userId, pilot)
    suspend fun readPilot(userId: String) = webDataSource.readPilot(userId)

    suspend fun incStatisticCounter(userId: String, document: String, field: String) =
        webDataSource.incStatisticCounter(userId, document, field)

    suspend fun readPurchase(purchaseToken: String) = webDataSource.readPurchase(purchaseToken)
    suspend fun updatePurchase(purchase: Purchase) = webDataSource.updatePurchase(purchase)

    companion object {
        @Volatile
        private var INSTANCE: AeroGlideRepository? = null

        fun getInstance(
            localDataSource: LocalDataSource,
            webDataSource: WebDataSource
        ): AeroGlideRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AeroGlideRepository(localDataSource, webDataSource)
                    .also { INSTANCE = it }
            }
    }
}
