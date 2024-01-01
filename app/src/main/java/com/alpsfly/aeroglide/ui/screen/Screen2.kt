package com.alpsfly.aeroglide.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alpsfly.aeroglide.core.common.hardware.SensorData
import com.alpsfly.aeroglide.viewmodel.SensorViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider

@Composable
fun Screen2(navController: NavController, sensorViewModel: SensorViewModel = hiltViewModel()) {
    val altitude by sensorViewModel.altitude.collectAsState(initial = SensorData())
    val climbrate by sensorViewModel.climbrate.collectAsState(initial = SensorData())
    val pressure by sensorViewModel.pressure.collectAsState(initial = SensorData())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        )
        {
            Text(text = "climbrate: ${climbrate.values[0]}@${climbrate.frequency}",
                modifier = Modifier.clickable {
                }
            )
            Chart(
                chart = lineChart(),
                chartModelProducer = sensorViewModel.climbrateChartEntryModelProducer,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
            )
            Text(text = "pressure: ${pressure.values[0]}@${pressure.frequency}",
                modifier = Modifier.clickable {
                }
            )
            Chart(
                chart = lineChart(
                    axisValuesOverrider = AxisValuesOverrider.adaptiveYValues(1.01f, true)
                ),
                chartModelProducer = sensorViewModel.pressureChartEntryModelProducer,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
            )
            Text(text = "altitude ${altitude.values[0]}@${altitude.frequency}",
                modifier = Modifier.clickable {
                }
            )
            Chart(
                chart = lineChart(
                    axisValuesOverrider = AxisValuesOverrider.adaptiveYValues(1.01f, true)
                ),
                chartModelProducer = sensorViewModel.altitudeChartEntryModelProducer,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
            )
        }
    }
}