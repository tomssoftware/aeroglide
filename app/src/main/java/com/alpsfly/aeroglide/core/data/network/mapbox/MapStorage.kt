package com.alpsfly.aeroglide.core.data.network.mapbox

import android.location.Location
import com.alpsfly.aeroglide.core.data.network.firebase.Response
import com.alpsfly.aeroglide.core.data.model.network.mapbox.Direction
import kotlinx.coroutines.flow.Flow

interface MapStorage {

    suspend fun loadRoute(origin: Location, destination: Location): Flow<Response<Direction?>>

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