package com.alpsfly.aeroglide.core.common.hardware

import com.alpsfly.aeroglide.core.common.timestamp

data class SensorData(
    val type: SensorType = SensorType.Unknown,
    val timestamp: Long = timestamp(),
    val values: FloatArray = floatArrayOf(0f, 0f, 0f)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SensorData

        if (type != other.type) return false
        if (timestamp != other.timestamp) return false
        if (!values.contentEquals(other.values)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + values.contentHashCode()
        return result
    }
}

enum class SensorType {
    Unknown,
    Acceleration,
    LinearAcceleration,
    VerticalAcceleration,
    Pressure,
    RotationVector,
    Altitude,
    Climbrate
}


