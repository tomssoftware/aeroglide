package com.alpsfly.aeroglide.core.hardware

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventCallback
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_NORMAL
import com.alpsfly.aeroglide.core.model.hardware.SensorData
import com.alpsfly.aeroglide.core.model.hardware.SensorType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest

/** Linear Acceleration sensor callback flow with SENSOR_DELAY_NORMAl, 200ms  */
fun SensorManager.linearAccelerationSensorDataFlow(enable: Flow<Boolean>) = callbackFlow {
    val callback = object : SensorEventCallback() {
        val frequency = SensorFrequency()
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val sensorData =
                    SensorData(
                        type = SensorType.LinearAcceleration,
                        frequency = frequency.inc(),
                        values = event.values.clone()
                    )
                this@callbackFlow.trySend(sensorData).isSuccess
            }
        }
    }

    enable.collectLatest { isEnabled ->
        if (isEnabled) {
            registerListener(callback, getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SENSOR_DELAY_NORMAL)
        } else {
            // todo: reset sensor data
            unregisterListener(callback)
        }
    }
    awaitClose {
        unregisterListener(callback)
    }
}

/** Pressure sensor callback flow with SENSOR_DELAY_NORMAl, 200ms  */
fun SensorManager.pressureSensorDataFlow(enable: Flow<Boolean>) = callbackFlow {
    val callback = object : SensorEventCallback() {
        val frequency = SensorFrequency()
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val sensorData =
                    SensorData(type = SensorType.Pressure, frequency = frequency.inc(), values = event.values.clone())
                this@callbackFlow.trySend(sensorData).isSuccess
            }
        }
    }

    enable.collectLatest { isEnabled ->
        if (isEnabled) {
            registerListener(callback, getDefaultSensor(Sensor.TYPE_PRESSURE), SENSOR_DELAY_NORMAL)
        } else {
            // todo: reset sensor data
            unregisterListener(callback)
        }
    }

    awaitClose {
        unregisterListener(callback)
    }
}

/** Rotation vector callback flow with SENSOR_DELAY_NORMAl, 200ms  */
fun SensorManager.rotationVectorSensorDataFlow(enable: Flow<Boolean>) = callbackFlow {
    val callback = object : SensorEventCallback() {
        val frequency = SensorFrequency()
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val sensorData = SensorData(
                    type = SensorType.RotationVector,
                    frequency = frequency.inc(),
                    values = event.values.clone()
                )
                this@callbackFlow.trySend(sensorData).isSuccess
            }
        }
    }
    enable.collectLatest { isEnabled ->
        if (isEnabled) {
            registerListener(callback, getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SENSOR_DELAY_NORMAL)
        } else {
            // todo: reset sensor data
            unregisterListener(callback)
        }
    }

    awaitClose {
        unregisterListener(callback)
    }
}
