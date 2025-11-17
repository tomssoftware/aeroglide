package com.alpsfly.aeroglide.core.domain.usecase

import android.app.Activity
import com.alpsfly.aeroglide.core.data.BillingRepository
import com.android.billingclient.api.ProductDetails
import javax.inject.Inject

class PurchaseUseCase @Inject constructor(
    private val billingRepository: BillingRepository
) {
    fun invoke(activity: Activity, productDetails: ProductDetails) {
        billingRepository.launchPurchaseFlow(activity, productDetails)
    }
}
