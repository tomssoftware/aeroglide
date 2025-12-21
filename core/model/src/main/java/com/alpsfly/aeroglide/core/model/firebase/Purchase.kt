package com.alpsfly.aeroglide.core.model.firebase

data class Purchase(
    var userId: String = "",
    var fcmToken: String = "",
    var purchaseToken: String = "",
    var subscriptionDetails: SubscriptionPurchase = SubscriptionPurchase()
)
