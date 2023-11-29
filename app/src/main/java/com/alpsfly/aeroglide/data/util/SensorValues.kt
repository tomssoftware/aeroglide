package com.alpsfly.aeroglide.data.util;

class SensorValues(val timestamp: Long = 0L, private val values: FloatArray = floatArrayOf(0f,0f,0f)) {
    var x: Float = values[0]
    set(value) {
        field = value
        values[0] = field
    }

    var y: Float = if (values.size > 1) { values[1] } else 0f
    set(value) {
        field = value
        values[1] = field
    }

    var z: Float = if (values.size > 2) { values[2] } else 0f
    set(value) {
        field = value
        values[2] = field
    }
}
