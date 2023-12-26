package com.alpsfly.aeroglide.data.repository

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.SensorEventCallback
import android.hardware.SensorManager
import android.location.Location
import androidx.compose.runtime.collectAsState
import com.alpsfly.aeroglide.data.util.SensorData
import com.alpsfly.aeroglide.data.util.pressureSensorDataFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface SensorRepository {
    /** Accelerometer sensor flow with 200ms delay */
    val accelerometerStateFlow: StateFlow<SensorData>
    fun updateAcceleration(acceleration: SensorData)

    /** Pressure sensor flow with 200ms delay */
    val pressureStateFlow: StateFlow<SensorData>
    fun updatePressure(pressure: SensorData)

    /** Location sensor flow with 1000ms delay */
    val locationStateFlow: StateFlow<Location>
    fun updateLocation(location: Location)
}

@Singleton
class SensorRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorRepository, SensorEventCallback() {
    /**
     * acceleration state flow
     */
    private val _accelerometerStateFlow = MutableStateFlow(SensorData())
    override val accelerometerStateFlow = _accelerometerStateFlow.asStateFlow()
    override fun updateAcceleration(acceleration: SensorData) {
        _accelerometerStateFlow.value = acceleration
    }

    /**
     * pressure state flow
     */
    private val _pressureStateFlow = MutableStateFlow(SensorData())
    override val pressureStateFlow: StateFlow<SensorData> = _pressureStateFlow.asStateFlow()
    override fun updatePressure(pressure: SensorData) {
        _pressureStateFlow.value = pressure
    }

    /**
     * location state flow
     */
    private val _locationStateFlow = MutableStateFlow(Location("none"))
    override val locationStateFlow: StateFlow<Location> = _locationStateFlow
    override fun updateLocation(location: Location) {
        _locationStateFlow.value = location
    }
}



