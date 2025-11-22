package com.alpsfly.aeroglide.feature.activityhistory

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.alpsfly.aeroglide.chart.LineChartScreen

@Composable
fun ClimbrateHistoryScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    activityId: Long,
    viewModel: ActivityViewModel = hiltViewModel(),
) {
    val uiState by viewModel.climbrateUiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is HistoryChartUiState.Loading -> {
                CircularProgressIndicator()
            }

            is HistoryChartUiState.NoData -> {
                Text("No climbrate data available for this flight.")
            }

            is HistoryChartUiState.Success -> {
                LineChartScreen(
                    modelProducer = viewModel.climbrateModelProducer,
                    xAxisFormatter = viewModel.xAxisLabelFormatter,
                    yAxisFormatter = viewModel.yAxisLabelFormatterClimbrate,
                    rangeProvider = viewModel.climbrateRangeProvider,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
