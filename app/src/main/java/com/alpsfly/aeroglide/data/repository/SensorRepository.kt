package com.alpsfly.aeroglide.data.repository

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.SensorManager
import com.alpsfly.aeroglide.data.util.accelSensorDataFlow
import com.alpsfly.aeroglide.data.util.chunked
import com.alpsfly.aeroglide.data.util.pressureSensorDataFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface SensorRepository {
    val accelDataSource: Flow<FloatArray>
    val pressureDataFlow: Flow<Float>
    val avgPressureFlow: Flow<Float>
}

class SensorRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorRepository {
    private val sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
    override val accelDataSource = sensorManager.accelSensorDataFlow()
    override val pressureDataFlow = sensorManager.pressureSensorDataFlow()
    override val avgPressureFlow: Flow<Float>
        get() {
            return pressureDataFlow.chunked(10) { it.sum() / 10 }
        }
}



