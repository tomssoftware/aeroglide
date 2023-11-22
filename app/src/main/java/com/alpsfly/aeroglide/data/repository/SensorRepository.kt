package com.alpsfly.aeroglide.data.repository

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.SensorManager
import com.alpsfly.aeroglide.data.util.accelSensorDataFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

interface SensorRepository {
    val accelDataSource: Flow<FloatArray>
}

class SensorRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorRepository {
    private val sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
    override val accelDataSource = sensorManager.accelSensorDataFlow()
}