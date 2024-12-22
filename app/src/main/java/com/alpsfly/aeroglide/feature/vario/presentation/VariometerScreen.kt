package com.alpsfly.aeroglide.feature.vario.presentation

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.hilt.navigation.compose.hiltViewModel
import com.alpsfly.aeroglide.core.data.util.SensorData
import com.alpsfly.aeroglide.core.viewmodel.SensorViewModel
import com.alpsfly.aeroglide.feature.vario.viewmodel.VarioViewModel


class CenteredModifier : DrawModifier {
    override fun ContentDrawScope.draw() {
        val center = Offset(size.width / 2, size.height / 2)
        translate(center.x, center.y) {
            this@draw.drawContent()
        }
    }
}

fun Modifier.centered() = this.then(CenteredModifier())

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
fun Variometer(modifier: Modifier, varioViewModel: VarioViewModel = hiltViewModel()) {
    val climbrate by varioViewModel.climbrate.collectAsState(initial = SensorData())

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

        drawClimbIndicator(this)
        drawVarioScale(this, majorOval, minorOval, climbrate.values[0])
        drawVarioText(this, textMeasurer, climbrate.values[0])
    }
}

fun DrawScope.drawClimbIndicator(drawScope: DrawScope) {
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
        drawPath(path = trianglePath, color = Color.Red)
    }
}

private fun DrawScope.drawVarioScale(drawScope: DrawScope, majorOval: Rect, minorOval: Rect, climbrate: Float) {
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

    val measuredText = textMeasurer.measure(climbrate.toString())

    withTransform({
        translate(-measuredText.size.width / 2f, -measuredText.size.height / 2f)
    }) {
        drawText(textMeasurer, climbrate.toString())
    }
}