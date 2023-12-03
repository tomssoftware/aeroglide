package com.alpsfly.aeroglide.data.repository

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import com.alpsfly.aeroglide.data.util.Limits
import com.alpsfly.aeroglide.data.util.SensorData
import com.alpsfly.aeroglide.data.util.accelSensorDataFlow
import com.alpsfly.aeroglide.data.util.chunked
import com.alpsfly.aeroglide.data.util.locationDataFlow
import com.alpsfly.aeroglide.data.util.pressureSensorDataFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

interface SensorRepository {
    /** Accelerometer sensor flow with 200ms delay */
    val accelerometerDataFlow: Flow<SensorData>

    /** Pressure sensor flow with 200ms delay */
    val pressureDataFlow: Flow<SensorData>

    /** Location sensor flow with 1000ms delay */
    val locationDataFlow: Flow<Location>
}

@Singleton
class SensorRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorRepository {
    private val sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    override val accelerometerDataFlow = sensorManager.accelSensorDataFlow()
    override val pressureDataFlow: Flow<SensorData>
        get() {
            val size = 5
            return sensorManager.pressureSensorDataFlow().chunked(size) { list ->
                val timestamp = list.fold(0L) { sum, item -> sum + item.timestamp } / list.size
                val pressure = list.fold(0f) { sum, item -> sum + item.values[0] } / list.size.toFloat()
                Timber.v("Send pressure data: $pressure @ $timestamp")
                SensorData(timestamp, floatArrayOf(pressure, 0f, 0f), list[0].type)
            }
        }
    override val locationDataFlow = locationManager.locationDataFlow(context, 1000L)
}



