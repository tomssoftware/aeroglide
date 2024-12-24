package com.alpsfly.aeroglide

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.alpsfly.aeroglide.core.data.database.LocalDataSource
import com.alpsfly.aeroglide.core.data.model.local.Altitude
import com.alpsfly.aeroglide.core.data.model.local.Climbrate
import com.alpsfly.aeroglide.core.data.model.local.Track
import com.alpsfly.aeroglide.core.data.model.local.TrackLog
import com.alpsfly.aeroglide.core.data.model.local.Velocity
import com.alpsfly.aeroglide.core.data.model.network.Pilot
import com.alpsfly.aeroglide.core.data.model.network.firebase.Blacklist
import com.alpsfly.aeroglide.core.data.model.network.firebase.Purchase
import com.alpsfly.aeroglide.core.data.model.network.firebase.User
import com.alpsfly.aeroglide.core.data.model.network.mapbox.Direction
import com.alpsfly.aeroglide.core.data.network.WebDataSource
import com.alpsfly.aeroglide.core.data.network.firebase.Response
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface IAeroGlideRepository {
//    val auth: FirebaseAuth
//    val currentUser: Flow<FirebaseUser?>
//    val allTracks: LiveData<MutableList<Track>>
//    val route: LiveData<Direction>

    suspend fun getTrack(trackId: Long): List<Track>

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

    suspend fun getUser(userId: String): List<com.alpsfly.aeroglide.core.data.model.local.User>

//    suspend fun loadRoute(origin: Location, destination: Location): Flow<Response<Direction?>>
//
//    suspend fun setRoute(route: Direction)
//
//    suspend fun readBlacklists(): Flow<Response<Blacklist?>>
//
//    suspend fun writeUser(userId: String, data: User): Flow<Response<Boolean>>
//
//    suspend fun writePilot(userId: String, pilot: Pilot): Flow<Response<Boolean>>
//
//    suspend fun readPilot(userId: String): Flow<Response<Pilot?>>
//
//    suspend fun incStatisticCounter(userId: String, document: String, field: String)
//
//    suspend fun readPurchase(purchaseToken: String): Flow<Response<Purchase?>>
//
//    suspend fun updatePurchase(purchase: Purchase)
}

@Singleton
class AeroGlideRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localDataSource: LocalDataSource,
    private val webDataSource: WebDataSource
) : IAeroGlideRepository {
//    override val auth = Firebase.auth
//
//    override val currentUser = callbackFlow {
//        val firebaseAuthStateListener = FirebaseAuth.AuthStateListener { auth ->
//            trySend(auth.currentUser)
//            if (auth.currentUser != null) {
//            }
//        }
//        auth.addAuthStateListener(firebaseAuthStateListener)
//        awaitClose {
//            auth.removeAuthStateListener(firebaseAuthStateListener)
//        }
//    }
//
//    override val allTracks: LiveData<MutableList<Track>> = localDataSource.allTracks

    override suspend fun getTrack(trackId: Long) = withContext(Dispatchers.IO) {
        localDataSource.getTrack(trackId)
    }

    override suspend fun getTrackClimbrates(trackId: Long) = withContext(Dispatchers.IO) {
        localDataSource.getTrackClimbrates(trackId)
    }

    override suspend fun getTrackLog(trackId: Long) = withContext(Dispatchers.IO) {
        localDataSource.getTrackLog(trackId)
    }

    override suspend fun getAvgSpeed(trackId: Long) = withContext(Dispatchers.IO) {
        localDataSource.getAvgSpeed(trackId) // todo: extend Track and calculate while recording
    }

    override suspend fun getMinAltitude(trackId: Long) = withContext(Dispatchers.IO) {
        localDataSource.getMinAltitude(trackId) // todo: extend Track calculate while recording
    }

    override suspend fun getTrackAltitudes(trackId: Long) = withContext(Dispatchers.IO) {
        localDataSource.getTrackAltitudes(trackId)
    }

    override suspend fun getTrackVelocities(trackId: Long) = withContext(Dispatchers.IO) {
        localDataSource.getTrackVelocities(trackId)
    }

    override suspend fun insertTrack(track: Track) = withContext(Dispatchers.IO) {
        localDataSource.insertTrack(track)
    }

    override suspend fun updateTrack(track: Track) = withContext(Dispatchers.IO) {
        localDataSource.updateTrack(track)
    }

    override suspend fun deleteTrack(track: Track) = withContext(Dispatchers.IO) {
        localDataSource.deleteTrack(track)
    }

    override suspend fun insertTrackLog(trackLog: TrackLog) = withContext(Dispatchers.IO) {
        localDataSource.insertTrackLog(trackLog)
    }

    override suspend fun getUser(userId: String) = withContext(Dispatchers.IO) {
        localDataSource.getUser(userId)
    }

//    override suspend fun loadRoute(origin: Location, destination: Location) = withContext(Dispatchers.IO) {
//        webDataSource.loadRoute(origin, destination)
//    }
//
//    private var _route = MutableLiveData(Direction())
////    override val route: LiveData<Direction>
////        get() = _route
//
//    override suspend fun setRoute(route: Direction) = withContext(Dispatchers.Main) {
//        _route.value = route
//    }
//
//    override suspend fun readBlacklists() = webDataSource.readBlacklists()
//
//    override suspend fun writeUser(userId: String, data: User) = webDataSource.writeUser(userId, data)
//
//    override suspend fun writePilot(userId: String, pilot: Pilot) = webDataSource.writePilot(userId, pilot)
//    override suspend fun readPilot(userId: String) = webDataSource.readPilot(userId)
//
//    override suspend fun incStatisticCounter(userId: String, document: String, field: String) =
//        webDataSource.incStatisticCounter(userId, document, field)
//
//    override suspend fun readPurchase(purchaseToken: String) = webDataSource.readPurchase(purchaseToken)
//    override suspend fun updatePurchase(purchase: Purchase) = webDataSource.updatePurchase(purchase)

//    companion object {
//        @Volatile
//        private var INSTANCE: AeroGlideRepository? = null
//
//        fun getInstance(
//            localDataSource: LocalDataSource,
//            webDataSource: WebDataSource
//        ): AeroGlideRepository =
//            INSTANCE ?: synchronized(this) {
//                INSTANCE ?: AeroGlideRepository(null, localDataSource, webDataSource)
//                    .also { INSTANCE = it }
//            }
//    }
}
