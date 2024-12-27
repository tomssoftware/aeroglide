package com.alpsfly.aeroglide.core.mapbox.data

import android.location.Location
import com.alpsfly.aeroglide.core.model.common.mapbox.Direction
import kotlinx.coroutines.flow.Flow

interface MapStorage {

    suspend fun loadRoute(origin: Location, destination: Location): Flow<com.alpsfly.aeroglide.core.firebase.Response<Direction?>>

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