package de.tomssoftware.aeroglide.feature.activityhistory

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
import de.tomssoftware.aeroglide.chart.LineChartScreen

@Composable
fun AltitudeHistoryScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    activityId: Long,
    viewModel: ActivityViewModel = hiltViewModel(),
) {
    val uiState by viewModel.altitudeUiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is HistoryChartUiState.Loading -> {
                CircularProgressIndicator()
            }

            is HistoryChartUiState.NoData -> {
                Text("No altitude data available for this flight.")
            }

            is HistoryChartUiState.Success -> {
                LineChartScreen(
                    modelProducer = viewModel.altitudeModelProducer,
                    xAxisFormatter = viewModel.xAxisLabelFormatter,
                    yAxisFormatter = viewModel.yAxisLabelFormatter,
                    rangeProvider = viewModel.altitudeRangeProvider,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
