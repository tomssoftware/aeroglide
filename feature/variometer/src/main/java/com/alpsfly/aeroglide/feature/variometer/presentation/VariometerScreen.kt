package com.alpsfly.aeroglide.feature.variometer.presentation

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alpsfly.aeroglide.core.common.units.LocalUnit
import com.alpsfly.aeroglide.core.common.units.UnitConverter
import com.alpsfly.aeroglide.core.model.hardware.SensorData
import com.alpsfly.aeroglide.core.presentation.AltitudeProfileScreen
import com.alpsfly.aeroglide.core.presentation.ClimbrateProfileScreen
import com.alpsfly.aeroglide.core.presentation.FlightStatusScreen
import com.alpsfly.aeroglide.core.presentation.centered
import com.alpsfly.aeroglide.core.presentation.AeroGlideBottomBar
import com.alpsfly.aeroglide.core.MenuItem
import com.alpsfly.aeroglide.core.mapbox.data.MapScreen
import com.alpsfly.aeroglide.core.model.database.Climbrate
import com.alpsfly.aeroglide.feature.variometer.viewmodel.VarioViewModel

private var startScaleAngle = 180f
private var climbrateAverage = 0f

private fun getClimbrateAverageAngle() = if (climbrateAverage >= 0f) {
    30f * climbrateAverage
} else {
    360f + 30f * climbrateAverage
}

private fun getOnScaleAngleStart(climbrate: Float) = if (climbrate < 0f) {
    startScaleAngle + 30f * climbrate
} else {
    startScaleAngle
}

private fun getOnScaleAngleEnd(climbrate: Float) = if (climbrate < 0f) {
    startScaleAngle + 1f
} else {
    startScaleAngle + 1f + 30f * climbrate
}

@Composable
fun VariometerScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        pageCount = { 4 }
    )
    val items: List<MenuItem> = listOf(
        MenuItem(
            id = "home",
            title = "Home",
            contentDescription = "Go to home screen",
            icon = Icons.Default.Home
        ),
        MenuItem(
            id = "settings",
            title = "Settings",
            contentDescription = "Go to settings screen",
            icon = Icons.Default.Settings
        ),
        MenuItem(
            id = "help",
            title = "Help",
            contentDescription = "Get help",
            icon = Icons.Default.Info
        ),
        MenuItem(
            id = "map",
            title = "Map",
            contentDescription = "Map",
            icon = Icons.Default.MailOutline
        ),
    )

    Scaffold(
        bottomBar = {
            AeroGlideBottomBar(
                modifier = Modifier,
                items = items,
                pagerState = pagerState,
                coroutineScope = coroutineScope
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            FlightStatusScreen(Modifier.fillMaxWidth(), navController)
            Spacer(Modifier.weight(2f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(it)
            ) {
                HorizontalPager(state = pagerState) { page ->
                    when (page) {
                        0 -> {
                            AnalogVariometer(
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        1 -> {
                            AltitudeProfileScreen(
                                modifier = Modifier.fillMaxSize(),
                                navController = navController
                            )
                        }

                        2 -> {
                            ClimbrateProfileScreen(
                                modifier = Modifier.fillMaxSize(),
                                navController = navController
                            )
                        }

                        3 -> {
                            MapScreen()
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun AnalogVariometer(
    modifier: Modifier = Modifier,
    varioViewModel: VarioViewModel = hiltViewModel()
) {
    val climbrate by varioViewModel.climbrate.collectAsState(initial = Climbrate())

    var majorOval: Rect
    var minorOval: Rect
    val majorScaleStrokeOff = remember {
        Paint().apply {
            isAntiAlias = true
            style = PaintingStyle.Fill
            color = Color.LightGray
            strokeWidth = 4f
        }
    }
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.centered()) {
        val radius = size.minDimension / 2.5f
        majorOval = Rect(-radius, -radius, radius, radius)
        minorOval = Rect(-radius, -radius, radius, radius)

        drawClimbIndicator(this, climbrate.climbrate)
        drawVarioScale(this, majorOval, minorOval, climbrate.climbrate)
        drawVarioText(this, textMeasurer, climbrate.climbrate)
    }
}

fun DrawScope.drawClimbIndicator(drawScope: DrawScope, climbrate: Float) {
    val radius = size.minDimension / 2.5f
    val indicatorScaleFactor = radius / 10f
    val majorOval = RectF().apply {
        left = -radius
        top = -radius
        right = radius
        bottom = radius
    }

    val trianglePath = Path().apply {
        reset()
        moveTo(-1f, 0f)
        lineTo(1f, 1f)
        lineTo(1f, -1f)
        close()
    }

    withTransform({
        translate(-majorOval.width() / 4f, -majorOval.height() / 15f)
        rotate(90f, Offset.Zero)
        scale(indicatorScaleFactor, indicatorScaleFactor, Offset.Zero)
    }) {
        drawPath(path = trianglePath, color = Color.LightGray)
    }

    withTransform({
        translate(-majorOval.width() / 4f, majorOval.height() / 15f)
        rotate(-90f, Offset.Zero)
        scale(indicatorScaleFactor, indicatorScaleFactor, Offset.Zero)
    }) {
        drawPath(path = trianglePath, color = Color.LightGray)
    }
}

private fun DrawScope.drawVarioScale(
    drawScope: DrawScope,
    majorOval: Rect,
    minorOval: Rect,
    climbrate: Float
) {
    // draw background scale
    val majorScale = Path()
    val minorScale = Path()
    val minScaleAngle = 20
    val maxScaleAngle = 340
    val sweepAngle = 1f

    (minScaleAngle until maxScaleAngle).forEach {
        if (it % 30 == 0) {
            majorScale.addArc(majorOval, it.toFloat(), sweepAngle)
        } else if (it % 3 == 0) {
            minorScale.addArc(minorOval, it.toFloat(), sweepAngle)
        }
    }
    drawPath(path = majorScale, color = Color.LightGray, style = Stroke(width = 48f))
    drawPath(path = minorScale, color = Color.LightGray, style = Stroke(width = 24f))

    // draw vario scale
    val onScaleAngleStart = getOnScaleAngleStart(climbrate = climbrate)
    val onScaleAngleEnd = getOnScaleAngleEnd(climbrate = climbrate)
    majorScale.reset()
    minorScale.reset()
    (onScaleAngleStart.toInt() until onScaleAngleEnd.toInt()).forEach {
        if (it % 30 == 0) {
            majorScale.addArc(majorOval, it.toFloat(), sweepAngle)
        } else if (it % 3 == 0) {
            minorScale.addArc(minorOval, it.toFloat(), sweepAngle)
        }
    }
    drawPath(path = majorScale, color = Color.Black, style = Stroke(width = 48f))
    drawPath(path = minorScale, color = Color.Black, style = Stroke(width = 24f))
}

private fun DrawScope.drawVarioText(
    drawScope: DrawScope,
    textMeasurer: TextMeasurer,
    climbrate: Float
) {
    val climbrateString = LocalUnit
        .of(climbrate, UnitConverter.Unit.MS)
        .withDigits(2)
        .withSymbol(true)
        .toLocalString()
    val measuredText = textMeasurer.measure(climbrateString)

    withTransform({
        translate(-measuredText.size.width / 2f, -measuredText.size.height / 2f)
    }) {
        drawText(textMeasurer, climbrateString)
    }
}