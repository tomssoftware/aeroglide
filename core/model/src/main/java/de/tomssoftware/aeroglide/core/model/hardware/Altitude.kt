package de.tomssoftware.aeroglide.core.model.hardware

data class Altitude(
    var timestamp: Long = 0L,
    var frequency: Float = 0f,
    var altitude: Float = 0f
)