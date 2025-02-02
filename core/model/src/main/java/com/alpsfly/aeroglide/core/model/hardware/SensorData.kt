package com.alpsfly.aeroglide.core.model.hardware

data class SensorData(
    val type: SensorType = SensorType.Unknown,
    val timestamp: Long = System.currentTimeMillis(),
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

    override fun toString(): String {
        return "type=$type\ntimestamp=$timestamp\nfrequency=$frequency\nvalues=${values.contentToString()}\n"
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



