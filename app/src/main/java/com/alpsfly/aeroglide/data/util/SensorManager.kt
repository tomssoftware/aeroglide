package com.alpsfly.aeroglide.data.util

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventCallback
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

fun SensorManager.accelSensorDataFlow() = callbackFlow {

    val callback = object : SensorEventCallback() {
        override fun onSensorChanged(event: SensorEvent?) {
            event ?: return
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val data = event.values.clone()
                this@callbackFlow.trySend(data).isSuccess
            }
        }
    }

    registerListener(callback, getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
    awaitClose {
        unregisterListener(callback)
    }
}

fun SensorManager.pressureSensorDataFlow() = callbackFlow {

    val callback = object : SensorEventCallback() {
        override fun onSensorChanged(event: SensorEvent?) {
            event ?: return
            if (event.sensor.type == Sensor.TYPE_PRESSURE) {
                val data = event.values.clone()
                this@callbackFlow.trySend(data[0]).isSuccess
            }
        }
    }

    registerListener(callback, getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL)
    awaitClose {
        unregisterListener(callback)
    }
}