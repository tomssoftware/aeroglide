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
                this@callbackFlow.trySend(SensorValues(System.currentTimeMillis(), event.values.clone())).isSuccess
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
                this@callbackFlow.trySend(SensorValues(System.currentTimeMillis(), event.values.clone())).isSuccess
            }
        }
    }

    registerListener(callback, getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL)
    awaitClose {
        unregisterListener(callback)
    }
}