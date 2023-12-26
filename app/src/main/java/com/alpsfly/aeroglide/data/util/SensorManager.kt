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
            event?.let {
                this@callbackFlow.trySend(getSensorData(it, startTime, count)).isSuccess
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
            event?.let {
                this@callbackFlow.trySend(getSensorData(it, startTime, count)).isSuccess
            }
        }
    }

    Timber.d("Register pressure sensor")
    registerListener(callback, getDefaultSensor(Sensor.TYPE_PRESSURE), SENSOR_DELAY_NORMAL)
    awaitClose {
        unregisterListener(callback)
    }
}

private fun getSensorData(event: SensorEvent, startTime: Long, count: Long): SensorData {
    val frequency = getFrequency(startTime, count)
    val sensorType = when (event.sensor.type) {
        Sensor.TYPE_ACCELEROMETER -> SensorType.Acceleration
        Sensor.TYPE_PRESSURE -> SensorType.Pressure
        else -> SensorType.Unknown
    }
    return SensorData(type = sensorType, frequency = frequency, values = event.values.clone())
}

private fun getFrequency(startTime: Long, count: Long): Float {
    val now = System.nanoTime()
    return (count / ((now - startTime) / 1000000000.0f))
}