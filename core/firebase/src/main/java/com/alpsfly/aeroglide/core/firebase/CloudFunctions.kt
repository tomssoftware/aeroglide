package com.alpsfly.aeroglide.core.firebase

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

