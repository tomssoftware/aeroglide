package com.alpsfly.aeroglide.data.util

data class SensorData(
    val timestamp: Long,
    val values: FloatArray,
    val type: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SensorData

        if (timestamp != other.timestamp) return false
        if (!values.contentEquals(other.values)) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + values.contentHashCode()
        result = 31 * result + type
        return result
    }
}

