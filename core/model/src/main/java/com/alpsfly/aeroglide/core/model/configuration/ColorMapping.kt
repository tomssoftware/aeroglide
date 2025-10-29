package com.alpsfly.aeroglide.core.model.configuration

import android.graphics.Color

class ColorMapping {
    val baseHueAscent = 90f
    val baseHueDecline = 150f
    val maxDecline = -4f
    val maxAscent = 6f
    val scale = 30f
    fun hueToColorString(hue: Float): String {
        val saturation = 1.0f
        val value = 1.0f
        val hsv = floatArrayOf(hue, saturation, value)
        val colorInt = Color.HSVToColor(hsv)
        return String.format("#%06X", (0xFFFFFF and colorInt))
    }

    fun climbrateToHue(climbrate: Float): Float {
        val baseHue = if (climbrate < 0) baseHueDecline else baseHueAscent
        var hue = baseHue + ((climbrate.coerceIn(maxDecline, maxAscent) * -1f) * scale)
        if (hue < 0f) {
            hue += 360f
        }
        return hue
    }

    fun getClimbrateColor(climbrate: Float): String {
        return hueToColorString(climbrateToHue(climbrate))
    }
}