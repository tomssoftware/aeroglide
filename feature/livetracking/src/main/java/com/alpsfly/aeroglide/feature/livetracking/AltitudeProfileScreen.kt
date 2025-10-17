package com.alpsfly.aeroglide.feature.livetracking

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.alpsfly.aeroglide.core.presentation.LineChartScreen
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

@Composable
fun AltitudeProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    altitudeProfileViewModel: AltitudeProfileViewModel = hiltViewModel()
) {
    Box(
        modifier = Modifier.fillMaxHeight()
    ) {
        LineChartScreen(
            modelProducer = altitudeProfileViewModel.altitudeModelProducer,
            rangeProvider = altitudeProfileViewModel.rangeProvider,
            modifier = Modifier
        )
    }
}
