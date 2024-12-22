package com.alpsfly.aeroglide.core.data.util

import timber.log.Timber

data class SensorData(
    val type: SensorType = SensorType.Unknown,
    val timestamp: Long = timestamp(),
    val frequency: Float = 0f,
    val values: FloatArray = floatArrayOf(0f, 0f, 0f)
) {
    init {
        //assert(type != SensorType.Unknown)
        //Timber.v("$timestamp ${type.name.substring(0, 4)} ${values.contentToString()}@$frequency Hz")
    }

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
    VerticalAcceleration,
    Pressure,
    RotationVector,
    Altitude,
    Climbrate
}

fun logSensorData(sensorData: SensorData) {
    Timber.v("${sensorData.timestamp} ${sensorData.type.name.substring(0, 4)} ${sensorData.values.contentToString()}@${sensorData.frequency} Hz")
}

