package com.alpsfly.aeroglide.core.model.container

import com.alpsfly.aeroglide.core.model.database.Altitude

data class AltitudeContainer(
    val altitude: Altitude,
    val min: Float = Float.MAX_VALUE,
    val max: Float = Float.MIN_VALUE,
    val avg: Float = 0f,
    val ascent: Float = 0f,
    val descent: Float = 0f
)