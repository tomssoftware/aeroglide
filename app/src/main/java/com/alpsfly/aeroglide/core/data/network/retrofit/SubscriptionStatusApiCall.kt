package com.thermalscout.appbase.datasource.remote.retrofit


import com.alpsfly.aeroglide.core.data.model.local.billing.ContentResource
import com.alpsfly.aeroglide.core.data.model.local.billing.SubscriptionStatus
import com.alpsfly.aeroglide.core.data.model.local.billing.SubscriptionStatusList
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
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

    // Fetch Basic content.
    @GET("content_basic")
    suspend fun fetchBasicContent(): ContentResource

    // Fetch Premium content.
    @GET("content_premium")
    suspend fun fetchPremiumContent(): ContentResource

    // Fetch Subscription Status.
    @GET("subscription_status")
    suspend fun fetchSubscriptionStatus(): Response<SubscriptionStatusList>

    // Registers subscription status to the server and get updated list of subscriptions
    @PUT("subscription_register")
    suspend fun registerSubscription(@Body registerStatus: SubscriptionStatus): Response<SubscriptionStatusList>

    // Transfers subscription status to another account.
    @PUT("subscription_transfer")
    suspend fun transferSubscription(@Body transferStatus: SubscriptionStatus): SubscriptionStatusList

    @PUT("acknowledge_purchase")
    suspend fun acknowledgeSubscription(@Body acknowledge: SubscriptionStatus): Response<SubscriptionStatusList>
}
