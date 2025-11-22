package com.alpsfly.aeroglide.feature.livetracking

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.alpsfly.aeroglide.chart.LineChartScreen

@Composable
fun AltitudeProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    altitudeProfileViewModel: AltitudeProfileViewModel = hiltViewModel()
) {
    val uiState by altitudeProfileViewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier.fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is ChartProfileUiState.Initial -> {
                // Only render the chart when we have data.
                LineChartScreen(
                    modelProducer = altitudeProfileViewModel.modelProducer,
                    rangeProvider = altitudeProfileViewModel.rangeProvider,
                    xAxisFormatter = altitudeProfileViewModel.xAxisLabelFormatter,
                    yAxisFormatter = altitudeProfileViewModel.yAxisLabelFormatter,
                    modifier = Modifier.fillMaxSize()
                )
            }

            is ChartProfileUiState.HasData -> {
                // Only render the chart when we have data.
                LineChartScreen(
                    modelProducer = altitudeProfileViewModel.modelProducer,
                    rangeProvider = altitudeProfileViewModel.rangeProvider,
                    xAxisFormatter = altitudeProfileViewModel.xAxisLabelFormatter,
                    yAxisFormatter = altitudeProfileViewModel.yAxisLabelFormatter,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
