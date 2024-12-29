package com.alpsfly.aeroglide.core.model.hardware

data class AltitudeCalibrationStatus(
    var isCalibrated: Boolean = false,
    var altitude0: Float = 0f,
    var pressure0: Float = 0f,
    var verticalAccuracy: Float = 0f,
    var horizontalAccuracy: Float = 0f
)
