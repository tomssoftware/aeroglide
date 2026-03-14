package com.alpsfly.aeroglide.core.model.hardware

data class Location(
    var timestamp: Long = 0L,
    var latitude: Float = 0f,
    var longitude: Float = 0f,
    var altitude: Float = 0f,
    var bearing: Float = 0f,
    var speed: Float = 0f,
    var geoidCorrection: Float = 0f,
    var hasHorizontalAccuracy: Boolean = false,
    var horizontalAccuracy: Float = 0f,
    var hasVerticalAccuracy: Boolean = false,
    var verticalAccuracy: Float = 0f,
    var bearingAccuracy: Float = 0f,
    var speedAccuracy: Float = 0f,
    var provider: String = "unknown"
)