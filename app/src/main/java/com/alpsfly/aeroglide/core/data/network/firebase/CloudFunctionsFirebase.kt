package com.thermalscout.appbase.datasource.remote.firebase

import com.alpsfly.aeroglide.BuildConfig
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.alpsfly.aeroglide.core.data.network.retrofit.PendingRequestCounter
import com.thermalscout.appbase.datasource.remote.retrofit.SubscriptionStatusApiCall
import com.thermalscout.appbase.datasource.remote.retrofit.authentication.RetrofitClient


class CloudFunctionsFirebase : CloudFunctions {

    val crashlytics = Firebase.crashlytics

    /**
     * Track the number of pending server requests.
     */
    private val pendingRequestCounter = PendingRequestCounter()

    private val firebaseFunctionsUrl: String = if (BuildConfig.DEBUG) {
        val address = BuildConfig.FIREBASE_EMULATOR_HOST_ADDRESS
        val port = BuildConfig.FIREBASE_EMULATOR_PORT_FUNCTIONS
        "http://$address:$port/thermalscout/us-central1/"
    } else {
        BuildConfig.FIREBASE_FUNCTIONS_URL // todo: check url
    }

    private val retrofitClient: RetrofitClient<SubscriptionStatusApiCall> = RetrofitClient(firebaseFunctionsUrl, SubscriptionStatusApiCall::class.java)

    override suspend fun registerInstanceId(instanceId: String) {
        val data = mapOf("instanceId" to instanceId)
        pendingRequestCounter.use {
            retrofitClient.getService().registerInstanceID(data)
        }
    }

    override suspend fun unregisterInstanceId(instanceId: String) {
        val data = mapOf("instanceId" to instanceId)
        pendingRequestCounter.use {
            retrofitClient.getService().unregisterInstanceID(data)
        }
    }

    override suspend fun verifyPurchase(purchaseToken: String): Boolean {
        val data = mapOf("purchaseToken" to purchaseToken)
        pendingRequestCounter.use {
            val response = retrofitClient.getService().verifyPurchase(data)
            if (!response.isSuccessful) {
                crashlytics.recordException(Exception("Verify purchase failed! ${response.message()}"))
            }
            if (response.body() == null) {
                crashlytics.recordException(Exception("Verify purchase failed! Empty response body received"))
            }
            return response.body() ?: false
        }
    }
}


