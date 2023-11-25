package com.alpsfly.aeroglide.data.repository

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.SensorManager
import com.alpsfly.aeroglide.data.util.SensorValues
import com.alpsfly.aeroglide.data.util.accelSensorDataFlow
import com.alpsfly.aeroglide.data.util.chunked
import com.alpsfly.aeroglide.data.util.pressureSensorDataFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface SensorRepository {
    val accelDataSource: Flow<SensorValues>
    val pressureDataFlow: Flow<SensorValues>
    val avgPressureFlow: Flow<SensorValues>
}

class SensorRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorRepository {
    private val sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
    override val accelDataSource = sensorManager.accelSensorDataFlow()
    override val pressureDataFlow = sensorManager.pressureSensorDataFlow()
    override val avgPressureFlow: Flow<SensorValues>
        get() {
            return pressureDataFlow.chunked(10) {  sensorValues ->
                val at = sensorValues.fold(0L) { sum, item -> sum + item.timestamp } / sensorValues.size
                val ax = sensorValues.fold(0f) { sum, item -> sum + item.x } / sensorValues.size.toFloat()
                val ay = sensorValues.fold(0f) { sum, item -> sum + item.y } / sensorValues.size.toFloat()
                val az = sensorValues.fold(0f) { sum, item -> sum + item.z } / sensorValues.size.toFloat()
                SensorValues(at, floatArrayOf(ax, ay, az) )
            }
        }
}



