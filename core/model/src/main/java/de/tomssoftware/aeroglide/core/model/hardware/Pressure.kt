package de.tomssoftware.aeroglide.core.model.hardware

data class Pressure(
    var timestamp: Long = 0L,
    var frequency: Float = 0f,
    var pressure: Float = 0f
)