package com.alpsfly.aeroglide.feature.analogvario

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alpsfly.aeroglide.core.common.hardware.SensorData
import com.alpsfly.aeroglide.core.common.hardware.SensorType
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import kotlin.math.min

@Composable
fun AnalogVarioScreen(
    navController: NavController,
    sensorViewModel: AnalogVarioViewModel = hiltViewModel()
) {
    val altitude =
        sensorViewModel.altitude.collectAsState(initial = SensorData(type = SensorType.Altitude))
    val climbrate =
        sensorViewModel.climbrate.collectAsState(initial = SensorData(type = SensorType.Climbrate))
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "this is ${altitude.value.values[0]}",
                modifier = Modifier
                    .clickable {
                        //navController.navigate(route = Screen.Screen1.route)
                    }
            )

            AnalogVario(climbrate)
            AltitudeGraph(navController = navController, sensorViewModel)
        }
    }
}

@Composable
fun AnalogVario(climbrateState: State<SensorData>) {
    val textMeasurer = rememberTextMeasurer()
    val colorPrimary = MaterialTheme.colorScheme.primary
    val typographyBodyMedium = MaterialTheme.typography.titleLarge

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            /**
             * Draw the vario scale
             */
            /**
             * Draw the vario scale
             */
            val sweepAngle = 1f
            val width = size.width
            val height = size.height
            val dimension = min(height, width)
            val centerX = dimension / 2f
            val centerY = dimension / 2f
            val radius = if (width > height) {
                height / 2.5f
            } else {
                width / 2.5f
            }
            val majorOval = Rect(-radius, -radius, radius, radius)
            val offset = 20.dp.toPx()
            val minorOval = Rect(-radius - offset, -radius - offset, radius + offset, radius + offset)

            translate(centerX, centerY) {
                val minScaleAngle = 20
                val maxScaleAngle = 340
                val majorScale = Path()
                val minorScale = Path()

                (minScaleAngle until maxScaleAngle).forEach {
                    if (it % 30 == 0) {
                        majorScale.addArc(majorOval, it.toFloat(), sweepAngle)
                    } else if (it % 3 == 0) {
                        minorScale.addArc(minorOval, it.toFloat(), sweepAngle)
                    }
                }

                drawPath(
                    color = colorPrimary,
                    path = majorScale,
                    style = Stroke(
                        width = 20.dp.toPx()
                    )
                )

                drawPath(
                    color = colorPrimary,
                    path = minorScale,
                    style = Stroke(
                        width = 10.dp.toPx()
                    )
                )
                /**
                 * Draw the vario climbrate text
                 */
                /**
                 * Draw the vario climbrate text
                 */
                val measuredText = textMeasurer.measure(
                    AnnotatedString(climbrateState.value.timestamp.toString())
                )

                drawText(
                    textMeasurer = textMeasurer,
                    text = climbrateState.value.values[0].toString(),
                    style = typographyBodyMedium,
//                    style = TextStyle(
//                        textAlign = TextAlign.Center,
//                        color = colorPrimary,
//                    ),
                    topLeft = Offset(-measuredText.size.width.toFloat() / 2f, -measuredText.size.height.toFloat() / 2f)
                )
                /**
                 * Draw the vario units text
                 */
                /**
                 * Draw the vario units text
                 */
            }
        }
    }
}

@Composable
fun AltitudeGraph(navController: NavController, sensorViewModel: AnalogVarioViewModel = hiltViewModel()) {
    val altitude by sensorViewModel.altitude.collectAsState(initial = SensorData())

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