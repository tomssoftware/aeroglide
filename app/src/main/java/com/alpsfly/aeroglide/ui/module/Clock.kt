package com.alpsfly.aeroglide.ui.module

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ClockComposable(
    hours: Int,
    minutes: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.minDimension / 2 - 20.dp.toPx()

        // Draw clock scale
        for (i in 0..59) {
            val angle = (i * PI / 30).toFloat()
            val lineLength = if (i % 5 == 0) 12.dp.toPx() else 8.dp.toPx()
            val lineWidth = if (i % 5 == 0) 2.dp.toPx() else 1.dp.toPx()
            val x1 = centerX + cos(angle) * (radius - lineLength)
            val y1 = centerY + sin(angle) * (radius - lineLength)
            val x2 = centerX + cos(angle) * radius
            val y2 = centerY + sin(angle) * radius
            drawLine(
                color = Color.Black,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = lineWidth,
                cap = StrokeCap.Round
            )
        }

        // Draw hour pointer
        val hourAngle = (hours % 12 + minutes / 60f) * PI / 6
        val hourPointerLength = radius * 0.6f
        val hourPointerX = centerX + cos(hourAngle) * hourPointerLength
        val hourPointerY = centerY + sin(hourAngle) * hourPointerLength
        drawLine(
            color = Color.Black,
            start = Offset(centerX, centerY),
            end = Offset(hourPointerX.toFloat(), hourPointerY.toFloat()),
            strokeWidth = 6.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Draw minute pointer
        val minuteAngle = (minutes % 60) * PI / 30
        val minutePointerLength = radius * 0.8f
        val minutePointerX = centerX + cos(minuteAngle) * minutePointerLength
        val minutePointerY = centerY + sin(minuteAngle) * minutePointerLength
        drawLine(
            color = Color.Black,
            start = Offset(centerX, centerY),
            end = Offset(minutePointerX.toFloat(), minutePointerY.toFloat()),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}