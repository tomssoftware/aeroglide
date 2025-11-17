package com.alpsfly.aeroglide.feature.billing

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.alpsfly.aeroglide.core.data.BillingRepository
import com.alpsfly.aeroglide.core.domain.usecase.PurchaseUseCase
import com.android.billingclient.api.ProductDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billingRepository: BillingRepository, // For observing state
    private val purchaseUseCase: PurchaseUseCase       // For triggering actions
) : ViewModel() {

    val availableProducts = billingRepository.availableProducts
    val isPremiumUser = billingRepository.isPremiumUser

    init {
        // Connect to the billing service as soon as the ViewModel is created.
        billingRepository.startBillingConnection()
    }

    fun onPurchaseClicked(activity: Activity, productDetails: ProductDetails) {
        purchaseUseCase.invoke(activity, productDetails)
    }
}
