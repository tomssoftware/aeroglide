package com.alpsfly.aeroglide.core.data.model.network.firebase

import com.thermalscout.database.network.firebase.SubscriptionPurchase

data class Purchase(
    var userId: String = "",
    var fcmToken: String = "",
    var purchaseToken: String = "",
    var subscriptionDetails: SubscriptionPurchase = SubscriptionPurchase()
)
