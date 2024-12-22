package com.alpsfly.aeroglide.feature.devicestatus.presentation

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alpsfly.aeroglide.core.data.util.SensorData
import com.alpsfly.aeroglide.core.ui.viewmodel.SensorViewModel
import com.alpsfly.aeroglide.feature.devicestatus.viewmodel.VicoChartViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer

@Composable
fun DeviceStatusScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    sensorViewModel: SensorViewModel = hiltViewModel(),
    vicoChartViewModel: VicoChartViewModel = hiltViewModel()
) {
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
            Text(text = "pressure: ${pressure.values[0]}@${pressure.frequency}",
                modifier = Modifier.clickable {
                }
            )
            ComposeChart1(vicoChartViewModel.pressureModelProducer, Modifier)

            Text(text = "altitude ${altitude.values[0]}@${altitude.frequency}",
                modifier = Modifier.clickable {
                }
            )
            ComposeChart1(vicoChartViewModel.altitudeModelProducer, Modifier)

            Text(text = "climbrate: ${climbrate.values[0]}@${climbrate.frequency}",
                modifier = Modifier.clickable {
                }
            )
            ComposeChart1(vicoChartViewModel.climbrateModelProducer, Modifier)
        }
    }
}

@Composable
private fun ComposeChart1(modelProducer: CartesianChartModelProducer, modifier: Modifier) {

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        remember { LineCartesianLayer.LineFill.single(fill(Color(0xffa485e0))) }
                    )
                ),
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                guideline = null,
                itemPlacer = remember { HorizontalAxis.ItemPlacer.aligned(30) },
            ),
        ),
        modelProducer = modelProducer,
        modifier = modifier,
        zoomState = rememberVicoZoomState(
            zoomEnabled = false,
            initialZoom = Zoom.x(60.0),

        ),
        scrollState = rememberVicoScrollState(
            scrollEnabled = true,
            autoScrollCondition = AutoScrollCondition.OnModelSizeIncreased,
            autoScroll = remember { Scroll.Relative.x(10.0) }
        )
    )
}

