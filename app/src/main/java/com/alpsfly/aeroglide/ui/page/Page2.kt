package com.alpsfly.aeroglide.ui.page

import android.hardware.Sensor
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
import com.alpsfly.aeroglide.data.util.SensorData
import com.alpsfly.aeroglide.viewmodel.SensorViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart

@Composable
fun Page2(sensorViewModel: SensorViewModel = hiltViewModel()) {
    val avgPressure by sensorViewModel.getPressureSenorData().collectAsState(initial = SensorData(0L, floatArrayOf(0f), Sensor.TYPE_PRESSURE))
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
            Text(text = "this is${avgPressure.values[0]}@${avgPressure.timestamp}",
                modifier = Modifier.clickable {
                }
            )
            Chart(
                chart = lineChart(),
                chartModelProducer = sensorViewModel.chartEntryModelProducer,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
            )
            Chart(
                chart = lineChart(),
                chartModelProducer = sensorViewModel.altitudeChartEntryModelProducer,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
            )
        }
    }
}

