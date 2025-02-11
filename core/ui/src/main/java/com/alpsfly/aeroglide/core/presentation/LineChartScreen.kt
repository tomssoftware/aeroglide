package com.alpsfly.aeroglide.core.presentation

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisTickComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.core.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider


@Composable
fun LineChartScreen(
    rangeProvider: CartesianLayerRangeProvider = CartesianLayerRangeProvider.auto(),
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier
) {
    val lineColor = Color(0xffa485e0)
    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(fill(lineColor)),
                        areaFill = LineCartesianLayer.AreaFill.single(
                            fill(
                                ShaderProvider.verticalGradient(
                                    arrayOf(lineColor.copy(alpha = 0.4f), Color.Transparent)
                                )
                            )
                        )
                    )
                ),
                rangeProvider = rangeProvider
            ),
            startAxis = VerticalAxis.rememberStart(
                // todo: configure
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                // todo: configure
                guideline = null
            ),
        ),
        modelProducer = modelProducer,
        modifier = modifier.fillMaxHeight(),
        zoomState = rememberVicoZoomState(
            zoomEnabled = false,
            initialZoom = Zoom.x(300.0),
        ),
        scrollState = rememberVicoScrollState(
            scrollEnabled = true,
            autoScrollCondition = AutoScrollCondition.OnModelGrowth,
            autoScroll = remember { Scroll.Relative.x(10.0) }
        )
    )
}
