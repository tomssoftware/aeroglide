package com.thermalscout.appbase.datasource.remote.firebase

import retrofit2.Response

interface CloudFunctions {
    suspend fun registerInstanceId(instanceId: String)
    suspend fun unregisterInstanceId(instanceId: String)
    suspend fun verifyPurchase(purchaseToken: String): Boolean

    companion object {
        @Volatile
        private var INSTANCE: CloudFunctions? = null

        fun getInstance(): CloudFunctions =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: CloudFunctionsFirebase().also {
                    INSTANCE = it
                }
            }
    }
}

