package com.alpsfly.aeroglide.data.util

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventCallback
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_NORMAL
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

/** Accelerometer sensor callback flow with SENSOR_DELAY_NORMAl, 200ms  */
fun SensorManager.accelerometerSensorDataFlow() = callbackFlow {
    val callback = object : SensorEventCallback() {
        val frequency = SensorFrequency()
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val sensorData = com.alpsfly.aeroglide.core.model.SensorData(
                    type = com.alpsfly.aeroglide.core.model.SensorType.Acceleration,
                    frequency = frequency.get(),
                    values = event.values.clone()
                )
                this@callbackFlow.trySend(sensorData).isSuccess
            }
        }
    }
    registerListener(callback, getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SENSOR_DELAY_NORMAL)
    awaitClose {
        unregisterListener(callback)
    }
}

/** Linear Acceleration sensor callback flow with SENSOR_DELAY_NORMAl, 200ms  */
fun SensorManager.linearAccelerationSensorDataFlow() = callbackFlow {
    val callback = object : SensorEventCallback() {
        val frequency = SensorFrequency()
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val sensorData = com.alpsfly.aeroglide.core.model.SensorData(
                    type = com.alpsfly.aeroglide.core.model.SensorType.LinearAcceleration,
                    frequency = frequency.get(),
                    values = event.values.clone()
                )
                this@callbackFlow.trySend(sensorData).isSuccess
            }
        }
    }
    registerListener(callback, getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SENSOR_DELAY_NORMAL)
    awaitClose {
        unregisterListener(callback)
    }
}

/** Pressure sensor callback flow with SENSOR_DELAY_NORMAl, 200ms  */
fun SensorManager.pressureSensorDataFlow() = callbackFlow {
    val callback = object : SensorEventCallback() {
        val frequency = SensorFrequency()
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val sensorData = com.alpsfly.aeroglide.core.model.SensorData(
                    type = com.alpsfly.aeroglide.core.model.SensorType.Pressure,
                    frequency = frequency.get(),
                    values = event.values.clone()
                )
                this@callbackFlow.trySend(sensorData).isSuccess
            }
        }
    }
    registerListener(callback, getDefaultSensor(Sensor.TYPE_PRESSURE), SENSOR_DELAY_NORMAL)
    awaitClose {
        unregisterListener(callback)
    }
}

/** Rotation vector callback flow with SENSOR_DELAY_NORMAl, 200ms  */
fun SensorManager.rotationVectorSensorDataFlow() = callbackFlow {
    val callback = object : SensorEventCallback() {
        val frequency = SensorFrequency()
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val sensorData = com.alpsfly.aeroglide.core.model.SensorData(
                    type = com.alpsfly.aeroglide.core.model.SensorType.RotationVector,
                    frequency = frequency.get(),
                    values = event.values.clone()
                )
                this@callbackFlow.trySend(sensorData).isSuccess
            }
        }
    }
    registerListener(callback, getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SENSOR_DELAY_NORMAL)
    awaitClose {
        unregisterListener(callback)
    }
}
