package com.alpsfly.aeroglide.data.util;

class SensorValues(val timestamp: Long, val values: FloatArray) {
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
