package de.tomssoftware.aeroglide.core.mapbox.data

import android.location.Location
import de.tomssoftware.aeroglide.core.model.common.mapbox.Direction
import kotlinx.coroutines.flow.Flow

interface MapStorage {

    suspend fun loadRoute(origin: Location, destination: Location): Flow<de.tomssoftware.aeroglide.core.firebase.Response<Direction?>>

    companion object {
        @Volatile
        private var INSTANCE: MapStorage? = null

        fun getInstance(): MapStorage =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: MapStorageMapbox().also {
                    INSTANCE = it
                }
            }
    }
}