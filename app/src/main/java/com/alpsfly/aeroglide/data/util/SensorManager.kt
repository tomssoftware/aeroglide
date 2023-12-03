package com.alpsfly.aeroglide.data.util

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventCallback
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_NORMAL
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber

/** Accelerometer sensor callback flow with SENSOR_DELAY_NORMAl, 200ms  */
fun SensorManager.accelSensorDataFlow() = callbackFlow {
    val callback = object : SensorEventCallback() {
        var startTime = 0L;
        var count = 0L;
        override fun onSensorChanged(event: SensorEvent?) {
            event ?: return

            if (startTime == 0L) {
                startTime = System.nanoTime()
            }
            val timestamp = System.nanoTime()
            val frequency = (count++ / ((timestamp - startTime) / 1000000000.0f))

            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val values = event.values.clone()
                Timber.v("Try to send accelerometer data: ${values[0]} @ $frequency")
                this@callbackFlow.trySend(SensorData(System.currentTimeMillis(), values, event.sensor.type)).isSuccess
            }
        }
    }
    Timber.d("Register accelerometer sensor")
    registerListener(callback, getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SENSOR_DELAY_NORMAL)
    awaitClose {
        unregisterListener(callback)
    }
}

/** Pressure sensor callback flow with SENSOR_DELAY_NORMAl, 200ms  */
fun SensorManager.pressureSensorDataFlow() = callbackFlow {
    val callback = object : SensorEventCallback() {
        var startTime = 0L;
        var count = 0L;
        override fun onSensorChanged(event: SensorEvent?) {
            event ?: return

            if (startTime == 0L) {
                startTime = System.nanoTime()
            }
            val timestamp = System.nanoTime()
            val frequency = (count++ / ((timestamp - startTime) / 1000000000.0f))

            if (event.sensor.type == Sensor.TYPE_PRESSURE) {
                val values = event.values.clone()
                Timber.v("Try to send pressure data: ${values[0]} @ $frequency")
                this@callbackFlow.trySend(SensorData(System.currentTimeMillis(), values, event.sensor.type)).isSuccess
            }
        }
    }
    Timber.d("Register pressure sensor")
    registerListener(callback, getDefaultSensor(Sensor.TYPE_PRESSURE), SENSOR_DELAY_NORMAL)
    awaitClose {
        unregisterListener(callback)
    }
}