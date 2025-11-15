package com.alpsfly.aeroglide.feature.activityhistory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.alpsfly.aeroglide.chart.LineChartScreen

@Composable
fun ClimbrateHistoryScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    activityId: Long,
    viewModel: ActivityViewModel = hiltViewModel(),
) {
    LaunchedEffect(key1 = activityId) {
        viewModel.loadClimbrates(activityId)
    }

    LineChartScreen(
        modelProducer = viewModel.climbrateModelProducer,
        xAxisFormatter = viewModel.xAxisLabelFormatter,
        yAxisFormatter = viewModel.yAxisLabelFormatterClimbrate,
        modifier = Modifier
    )
}