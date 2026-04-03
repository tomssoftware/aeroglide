package de.tomssoftware.aeroglide.feature.billing

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@Composable
fun PremiumScreen(
    navController: NavController,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val activity = LocalActivity.current as Activity
    val products by viewModel.availableProducts.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremiumUser.collectAsStateWithLifecycle()

    Column {
        if (isPremium) {
            Text("Thank you for being a Premium User!")
        } else {
            Text("Unlock Premium Features")
            products.forEach { product ->
                Button(onClick = { viewModel.onPurchaseClicked(activity, product) }) {
                    Text("Buy ${product.name} for ${product.oneTimePurchaseOfferDetails?.formattedPrice}")
                }
            }
        }
    }
}
