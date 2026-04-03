package de.tomssoftware.aeroglide.core.data

import android.app.Activity
import android.content.Context
import de.tomssoftware.aeroglide.core.common.di.ApplicationScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

// Define the product IDs. This is where you list your in-app products from the Play Console.
object ProductIds {
    const val PREMIUM_UNLOCK = "aeroglide_premium_unlock" // Example ID for a one-time purchase
}

// Interface for testability and abstraction
interface BillingRepository {
    val availableProducts: StateFlow<List<ProductDetails>>
    val isPremiumUser: StateFlow<Boolean>
    fun startBillingConnection()
    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails)
}

@Singleton
class BillingRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:ApplicationScope private val externalScope: CoroutineScope
) : BillingRepository, PurchasesUpdatedListener, BillingClientStateListener {

    private val _availableProducts = MutableStateFlow<List<ProductDetails>>(emptyList())
    override val availableProducts = _availableProducts.asStateFlow()

    private val _isPremiumUser = MutableStateFlow(false)
    override val isPremiumUser = _isPremiumUser.asStateFlow()

    private lateinit var billingClient: BillingClient

    override fun startBillingConnection() {
        if (::billingClient.isInitialized && billingClient.isReady) {
            Timber.d("BillingClient already connected.")
            externalScope.launch {
                queryProducts()
                queryPurchases()
            }
            return
        }

        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()

        Timber.d("Starting BillingClient connection...")
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Timber.i("BillingClient setup successful.")
            externalScope.launch {
                queryProducts()
                queryPurchases()
            }
        } else {
            Timber.e("BillingClient setup failed: ${billingResult.debugMessage}")
        }
    }

    override fun onBillingServiceDisconnected() {
        Timber.w("BillingClient disconnected. Retrying...")
        // Try to restart the connection on the next retry.
        // A backoff strategy would be even better in a production app.
        startBillingConnection()
    }

    private suspend fun queryProducts() {
        if (!billingClient.isReady) {
            Timber.e("queryProducts: BillingClient is not ready")
            return
        }

        val productList = listOf(
            Product.newBuilder()
                .setProductId(ProductIds.PREMIUM_UNLOCK)
                .setProductType(BillingClient.ProductType.INAPP) // For one-time purchases
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
        val (billingResult, productDetailsList) = billingClient.queryProductDetails(params)

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList != null) {
            _availableProducts.value = productDetailsList
            Timber.i("Available products queried: $productDetailsList")
        } else {
            Timber.e("Failed to query products: ${billingResult.debugMessage}")
        }
    }

    override fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        if (!billingClient.isReady) {
            Timber.e("BillingClient not ready to launch purchase flow.")
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else {
            Timber.e("Purchase failed or was cancelled: ${billingResult.debugMessage}")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        // This is the key part for backend-readiness.
        // For now, we verify locally.
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                // TODO:
                // In a production app with a backend, you would send purchase.purchaseToken
                // to your server here for verification and to grant entitlement.
                // Your server would then call the Google Play Developer API to verify and acknowledge.

                // For a backend-less app, we acknowledge the purchase directly.
                // WARNING: This is not secure against fraud. A user could potentially spoof this.
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Timber.i("Purchase acknowledged successfully.")
                        // Grant entitlement to the user
                        _isPremiumUser.value = true
                    } else {
                        Timber.e("Failed to acknowledge purchase: ${billingResult.debugMessage}")
                    }
                }
            } else {
                // If already acknowledged, the user owns the item. Grant entitlement.
                _isPremiumUser.value = true
            }
        }
    }

    private fun queryPurchases() {
        if (!billingClient.isReady) return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    if (purchase.isAcknowledged && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        _isPremiumUser.value = true
                        return@queryPurchasesAsync
                    }
                }
            }
            // If we reach here, no active premium purchase was found.
            _isPremiumUser.value = false
        }
    }
}
