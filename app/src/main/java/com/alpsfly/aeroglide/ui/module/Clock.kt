package com.alpsfly.aeroglide.ui.module

import android.content.res.Resources
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun ClockComposable() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val radius = 100.dp
        val center = Offset(radius.toPx(), radius.toPx())

        // Draw the clock face
        Canvas(modifier = Modifier.size(radius * 2)) {
            drawCircle(
                center = center,
                radius = radius.toPx(),
                color = Color.LightGray
            )
        }

        // Draw the hour hand
        val hourAngle = calculateAngle(hour = 12, minutes = 0)
        val hourHandLength = radius.toPx() * 0.6
        val hourHandStart = center.copy(x = (center.x - hourHandLength / 2).toFloat(), y = (center.y - hourHandLength / 2).toFloat())
        val hourHandEnd = center.copy(
            x = (center.x + hourHandLength / 2 * Math.cos(hourAngle.toDouble())).toFloat(),
            y = (center.y + hourHandLength / 2 * Math.sin(hourAngle.toDouble())).toFloat()
        )

        Canvas(modifier = Modifier.size(radius * 2)) {
            drawLine(
                start = hourHandStart,
                end = hourHandEnd,
                color = Color.Black,
                strokeWidth = 5f
            )
        }

        // Draw the minute hand
        val minuteAngle = calculateAngle(hour = 0, minutes = 30)
        val minuteHandLength = radius.toPx() * 0.8
        val minuteHandStart = center.copy(x = (center.x - minuteHandLength / 2).toFloat(), y = (center.y - minuteHandLength / 2).toFloat())
        val minuteHandEnd = center.copy(
            x = (center.x + minuteHandLength / 2 * Math.cos(minuteAngle.toDouble())).toFloat(),
            y = (center.y + minuteHandLength / 2 * Math.sin(minuteAngle.toDouble())).toFloat()
        )

        Canvas(modifier = Modifier.size(radius * 2)) {
            drawLine(
                start = minuteHandStart,
                end = minuteHandEnd,
                color = Color.Black,
                strokeWidth = 3f
            )
        }

        // Draw the second hand
        val secondAngle = calculateAngle(hour = 0, minutes = 0, seconds = 30)
        val secondHandLength = radius.toPx() * 0.9
        val secondHandStart = center.copy(x = (center.x - secondHandLength / 2).toFloat(), y = (center.y - secondHandLength / 2).toFloat())
        val secondHandEnd = center.copy(
            x = (center.x + secondHandLength / 2 * Math.cos(secondAngle.toDouble())).toFloat(),
            y = (center.y + secondHandLength / 2 * Math.sin(secondAngle.toDouble())).toFloat()
        )

        Canvas(modifier = Modifier.size(radius * 2)) {
            drawLine(
                start = secondHandStart,
                end = secondHandEnd,
                color = Color.Red,
                strokeWidth = 2f
            )
        }
    }
}

private fun calculateAngle(hour: Int, minutes: Int, seconds: Int = 0): Int {
    return 0
}

private fun Dp.toPx(): Float {
    val displayMetrics = Resources.getSystem().displayMetrics
    val density = displayMetrics.density
    val px = 100 * density

    return px
}
