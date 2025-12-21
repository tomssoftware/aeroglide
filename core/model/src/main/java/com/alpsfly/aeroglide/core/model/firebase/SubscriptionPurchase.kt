package com.alpsfly.aeroglide.core.model.firebase

// todo: add missing fields
data class SubscriptionPurchase(
    var acknowledgementState: String = "",
    var subscriptionState: String = "",
    var latestOrderId: String = ""
)
