package com.alpsfly.aeroglide.data.util

data class SensorData(
    val type: SensorType = SensorType.Unknown,
    val timestamp: Long = timestamp(),
    val frequency: Float = 0f,
    val values: FloatArray = floatArrayOf(0f, 0f, 0f)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SensorData

        if (type != other.type) return false
        if (timestamp != other.timestamp) return false
        if (frequency != other.frequency) return false
        if (!values.contentEquals(other.values)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + frequency.hashCode()
        result = 31 * result + values.contentHashCode()
        return result
    }
}

enum class SensorType {
    Unknown,
    Acceleration,
    LinearAcceleration,
    Pressure,
    Altitude
}

