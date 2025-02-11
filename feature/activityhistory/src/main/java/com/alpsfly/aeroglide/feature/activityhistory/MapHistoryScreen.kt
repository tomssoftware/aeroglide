package com.alpsfly.aeroglide.feature.activityhistory

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alpsfly.aeroglide.core.presentation.LineChartScreen


@Composable
fun MapHistoryScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: ActivityViewModel = hiltViewModel(),
) {
    LineChartScreen(
        modelProducer = viewModel.altitudeModelProducer,
        modifier = Modifier
    )
}