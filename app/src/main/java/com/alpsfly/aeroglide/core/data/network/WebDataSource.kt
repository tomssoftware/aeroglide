package com.alpsfly.aeroglide.core.data.network

import android.location.Location
import com.alpsfly.aeroglide.core.data.network.firebase.CloudFunctions
import com.alpsfly.aeroglide.core.data.network.firebase.CloudStorage
import com.alpsfly.aeroglide.core.data.network.mapbox.MapStorage
import com.alpsfly.aeroglide.core.data.model.network.Pilot
import com.alpsfly.aeroglide.core.data.model.network.firebase.Purchase
import com.alpsfly.aeroglide.core.data.model.network.firebase.User

class WebDataSource(
    private val cloudStorage: CloudStorage,
    private val cloudFunctions: CloudFunctions,
    private val mapStorage: MapStorage
) {
    suspend fun readBlacklists() = cloudStorage.readBlacklists()

    suspend fun writeUser(userId: String, data: User) = cloudStorage.writeUser(userId, data)

    suspend fun writePilot(userId: String, pilot: Pilot) = cloudStorage.writePilot(userId, pilot)
    suspend fun readPilot(userId: String) = cloudStorage.readPilot(userId)

    suspend fun incStatisticCounter(userId: String, document: String, field: String) = cloudStorage.incStatisticCounter(userId, document, field)

    suspend fun readPurchase(purchaseToken: String) = cloudStorage.readPurchase(purchaseToken)
    suspend fun updatePurchase(purchase: Purchase) = cloudStorage.updatePurchase(purchase)

    suspend fun loadRoute(origin: Location, destination: Location) = mapStorage.loadRoute(origin, destination)


    companion object {

        @Volatile
        private var INSTANCE: WebDataSource? = null

        fun getInstance(cloudStorage: CloudStorage, cloudFunctions: CloudFunctions, mapStorage: MapStorage): WebDataSource =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: WebDataSource(cloudStorage, cloudFunctions, mapStorage).also {
                    INSTANCE = it
                }
            }
    }
}