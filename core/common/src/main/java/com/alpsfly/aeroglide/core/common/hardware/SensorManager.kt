package com.alpsfly.aeroglide.core.common.hardware

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventCallback
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_FASTEST
import android.hardware.SensorManager.SENSOR_DELAY_GAME
import com.alpsfly.aeroglide.core.common.filter.LowPassFilter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

/** Accelerometer sensor callback flow with SENSOR_DELAY_NORMAl, 200ms  */
fun SensorManager.accelerometerSensorDataFlow() = callbackFlow {
    val callback = object : SensorEventCallback() {
        val filter = LowPassFilter(0.72f)
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val filteredValues = filter.filter(it.values.clone(), System.nanoTime())
                val sensorData = SensorData(
                    type = SensorType.Acceleration,
                    values = filteredValues
                )
                this@callbackFlow.trySend(sensorData).isSuccess
            }
        }
    }
    registerListener(callback, getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SENSOR_DELAY_FASTEST)
    awaitClose {
        unregisterListener(callback)
    }
}

/** Linear Acceleration sensor callback flow with SENSOR_DELAY_NORMAl, 200ms  */
fun SensorManager.linearAccelerationSensorDataFlow() = callbackFlow {
    val callback = object : SensorEventCallback() {
        val filter = LowPassFilter(0.72f)
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val filteredValues = filter.filter(it.values.clone(), System.nanoTime())
                val sensorData = SensorData(
                    type = SensorType.LinearAcceleration,
                    values = filteredValues
                )
                this@callbackFlow.trySend(sensorData).isSuccess
            }
        }
    }
    registerListener(callback, getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SENSOR_DELAY_FASTEST)
    awaitClose {
        unregisterListener(callback)
    }
}

/** Pressure sensor callback flow with SENSOR_DELAY_NORMAl, 200ms  */
fun SensorManager.pressureSensorDataFlow() = callbackFlow {
    val callback = object : SensorEventCallback() {
        val filter = LowPassFilter(0.2f)
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val filteredValues = filter.filter(it.values.clone(), System.nanoTime())
                val sensorData = SensorData(
                    type = SensorType.Pressure,
                    values = filteredValues
                )
                this@callbackFlow.trySend(sensorData).isSuccess
            }
        }
    }
    registerListener(callback, getDefaultSensor(Sensor.TYPE_PRESSURE), SENSOR_DELAY_GAME)
    awaitClose {
        unregisterListener(callback)
    }
}

/** Rotation vector callback flow with SENSOR_DELAY_NORMAl, 200ms  */
fun SensorManager.rotationVectorSensorDataFlow() = callbackFlow {
    val callback = object : SensorEventCallback() {
        val filter = LowPassFilter(0.72f)
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val filteredValues = filter.filter(it.values.clone(), System.nanoTime())
                val sensorData = SensorData(
                    type = SensorType.RotationVector,
                    values = filteredValues
                )
                this@callbackFlow.trySend(sensorData).isSuccess
            }
        }
    }
    registerListener(callback, getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SENSOR_DELAY_FASTEST)
    awaitClose {
        unregisterListener(callback)
    }
}
