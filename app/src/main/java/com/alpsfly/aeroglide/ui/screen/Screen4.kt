package com.alpsfly.aeroglide.ui.screen

//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import kotlinx.coroutines.launch
//
//@Composable
//fun LineChartExample() {
//    val chartModel = remember { CartesianChartModel() }
//    val scope = rememberCoroutineScope()
//    val values = remember { mutableListOf<Float>() }
//    val floatFlow = floatValuesFlow()
//
//    LaunchedEffect(Unit) {
//        floatFlow.collect { value ->
//            values.add(value)
//            if (values.size > 300) {
//                values.removeAt(0)
//            }
//            scope.launch {
//                chartModel.runTransaction {
//                    lineSeries {
//                        series(values)
//                    }
//                }
//            }
//        }
//    }
//
//    CartesianChartHost(
//        chart = chartModel,
//        modifier = Modifier.fillMaxSize(),
//        xAxis = XAxis(
//            majorTickCount = 6,
//            tickConfig = XAxis.TickConfig(),
//            labelConfig = XAxis.LabelConfig(spacing = 60)
//        ),
//        yAxis = YAxis(),
//        series = lineSeries {
//            line {
//                dataPoints(values)
//            }
//        }
//    )
//}
