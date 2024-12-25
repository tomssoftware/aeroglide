package com.alpsfly.aeroglide.core.data.network.firebase

import com.alpsfly.aeroglide.core.data.model.network.Pilot
import com.alpsfly.aeroglide.core.data.model.network.firebase.User
import com.alpsfly.aeroglide.core.data.model.network.firebase.Blacklist
import com.alpsfly.aeroglide.core.data.model.network.firebase.Purchase
import kotlinx.coroutines.flow.Flow

interface CloudStorage {
    suspend fun readBlacklists(): Flow<Response<Blacklist?>>
    suspend fun readPurchase(purchaseToken: String): Flow<Response<Purchase?>>

    suspend fun writeUser(userId: String, data: User): Flow<Response<Boolean>>
    suspend fun addUserCallbackCollector(userId: String): Flow<Response<User?>>

    suspend fun writePilot(userId: String, data: Pilot): Flow<Response<Boolean>>
    suspend fun readPilot(userId: String): Flow<Response<Pilot?>>
    suspend fun addPilotCallbackCollector(userId: String): Flow<Response<Pilot?>>

    suspend fun incStatisticCounter(userId: String, documentName: String, fieldName: String)
    suspend fun updatePurchase(purchase: Purchase)

    companion object {

        // root collection paths
        const val USERS = "users"
        const val PILOTS = "pilots"
        const val PURCHASES = "purchases"
        const val STATISTICS = "statistics"
        const val CONFIGS = "configs"

        // documents
        const val REQUESTS = "requests"
        const val BLACKLIST = "blacklist"

        // fields
        const val DIRECTION_CALL_COUNTER = "directionCallCounter"
        const val VERSIONS = "versions"
    }
}
