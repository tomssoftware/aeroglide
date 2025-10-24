package com.alpsfly.aeroglide.core.model.configuration

import android.graphics.Color

class ColorMapping {
    fun hueToColorString(hue: Float): String {
        val saturation = 1.0f
        val value = 1.0f
        val hsv = floatArrayOf(hue, saturation, value)
        val colorInt = Color.HSVToColor(hsv)
        return String.format("#%06X", (0xFFFFFF and colorInt))
    }

    fun climbrateToHue(climbrate: Float): Float {
        val baseHue = if (climbrate < 0) 150f else 90f
        var hue = baseHue + ((climbrate.coerceIn(-4f, 6f) * -1f) * 30f)
        if (hue < 0f) {
            hue += 360f
        }
        return hue
    }

    fun getClimbrateColor(climbrate: Float): String {
        return hueToColorString(climbrateToHue(climbrate))
    }
}