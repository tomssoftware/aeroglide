package com.alpsfly.aeroglide.core.data.network

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface IWebDataSource {
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
//
//    suspend fun loadRoute(origin: Location, destination: Location): Flow<Response<Direction?>>
}

@Singleton
class WebDataSource @Inject constructor(
//    @ApplicationContext private val context: Context,
//    private val cloudStorage: CloudStorage,
//    private val cloudFunctions: CloudFunctions,
//    private val mapStorage: MapStorage
) : IWebDataSource {
//    override suspend fun readBlacklists() = cloudStorage.readBlacklists()
//
//    override suspend fun writeUser(userId: String, data: User) =
//        cloudStorage.writeUser(userId, data)
//
//    override suspend fun writePilot(userId: String, pilot: Pilot) =
//        cloudStorage.writePilot(userId, pilot)
//
//    override suspend fun readPilot(userId: String) = cloudStorage.readPilot(userId)
//
//    override suspend fun incStatisticCounter(userId: String, document: String, field: String) =
//        cloudStorage.incStatisticCounter(userId, document, field)
//
//    override suspend fun readPurchase(purchaseToken: String) =
//        cloudStorage.readPurchase(purchaseToken)
//
//    override suspend fun updatePurchase(purchase: Purchase) = cloudStorage.updatePurchase(purchase)
//
//    override suspend fun loadRoute(origin: Location, destination: Location) =
//        mapStorage.loadRoute(origin, destination)


}
