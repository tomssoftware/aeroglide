package de.tomssoftware.aeroglide.core.firebase.retrofit


import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PUT

/**
 * [SubscriptionStatusApiCall] defines the API endpoints that are called in [ServerFunctionsImpl].
 */
interface SubscriptionStatusApiCall { // todo: cleanup

    // Registers Instance ID for Firebase Cloud Messaging.
    @PUT("registerInstanceId")
    suspend fun registerInstanceID(@Body instanceId: Map<String, String>): String

    // Unregisters Instance ID for Firebase Cloud Messaging.
    @PUT("unregisterInstanceId")
    suspend fun unregisterInstanceID(@Body instanceId: Map<String, String>): String

    // Purchase verification.
    @PUT("verifyPurchase")
    suspend fun verifyPurchase(@Body purchaseParams: Map<String, String>): Response<Boolean>
}
