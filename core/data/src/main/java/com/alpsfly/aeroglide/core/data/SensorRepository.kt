package com.alpsfly.aeroglide.core.data

import android.content.Context
import android.hardware.SensorEventCallback
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import com.alpsfly.aeroglide.core.domain.IKalmanFilter
import com.alpsfly.aeroglide.core.domain.KalmanFilter
import com.alpsfly.aeroglide.core.common.Limits
import com.alpsfly.aeroglide.core.common.chunked
import com.alpsfly.aeroglide.core.domain.Q_ACCELERATION
import com.alpsfly.aeroglide.core.domain.R_ALTITUDE
import com.alpsfly.aeroglide.core.hardware.accelerometerSensorDataFlow
import com.alpsfly.aeroglide.core.domain.getVerticalAcceleration
import com.alpsfly.aeroglide.core.hardware.SensorFrequency
import com.alpsfly.aeroglide.core.hardware.linearAccelerationSensorDataFlow
import com.alpsfly.aeroglide.core.hardware.locationDataFlow
import com.alpsfly.aeroglide.core.hardware.pressureSensorDataFlow
import com.alpsfly.aeroglide.core.hardware.rotationVectorSensorDataFlow
import com.alpsfly.aeroglide.core.model.SensorData
import com.alpsfly.aeroglide.core.model.SensorType
import com.alpsfly.aeroglide.core.model.hardware.Calibration
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

interface SensorRepository {
    /** Accelerometer sensor flow with 200ms delay */
    val accelerometerDataSource: Flow<SensorData>

    /** Linear acceleration sensor flow with 200ms delay */
    val linearAccelerationDataSource: Flow<SensorData>

    /** Pressure sensor flow with 200ms delay */
    val pressureDataSource: Flow<SensorData>

    /** Rotation vector sensor flow with 200ms delay */
    val rotationVectorDataSource: Flow<SensorData>

    /** Location sensor flow with 1000ms delay */
    val locationDataSource: Flow<Location>

    /** Fused sensor */
    val verticalAccelerationFlow: Flow<SensorData>

    /** Fused sensor */
    val climbRateFlow: Flow<SensorData>

    /** Fused sensor */
    val altitudeFlow: Flow<SensorData>

    /** Status of altitude calibration **/
    val altitudeCalibrationStatus: StateFlow<Calibration>
}

@Singleton
class SensorRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SensorRepository, SensorEventCallback() {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val kalmanFilter: IKalmanFilter =
        KalmanFilter(
            Q_ACCELERATION,
            R_ALTITUDE
        )

    /**
     * acceleration state flow
     */
    override val accelerometerDataSource = sensorManager.accelerometerSensorDataFlow().shareSensorData()

    /**
     * Linear acceleration state flow
     */
    override val linearAccelerationDataSource = sensorManager.linearAccelerationSensorDataFlow().shareSensorData()

    /**
     * pressure state flow
     */
    override val pressureDataSource = sensorManager.pressureSensorDataFlow().shareSensorData()

    /**
     * Rotation vector state flow
     */
    override val rotationVectorDataSource = sensorManager.rotationVectorSensorDataFlow().shareSensorData()

    /**
     * Location state flow
     */
    override val locationDataSource = locationManager.locationDataFlow(context, 1000).shareSensorData()

    /**
     * Vertical acceleration flow
     */
    private val targetFrequency = 5f
    private val sourceFrequency = SensorFrequency()
    private fun chunkSize() =
        if (sourceFrequency.get() / targetFrequency <= 0f) 10 else (sourceFrequency.get() / targetFrequency).toInt()

    override val verticalAccelerationFlow: Flow<SensorData>
        get() {
            return combine(
                sensorManager.linearAccelerationSensorDataFlow(),
                sensorManager.rotationVectorSensorDataFlow()
            ) { a, r ->
                getVerticalAcceleration(a, r)
            }.chunked(chunkSize()) { verticalAcceleration ->
                verticalAcceleration.average().toFloat()
            }.map { averageVerticalAcceleration ->
                SensorData(
                    type = SensorType.VerticalAcceleration,
                    frequency = sourceFrequency.get(),
                    values = floatArrayOf(averageVerticalAcceleration)
                )
            }
        }

    /**
     * Altitude flow
     */
    private val _altitudeCalibrationStatus = MutableStateFlow(Calibration())
    override val altitudeCalibrationStatus: StateFlow<Calibration> = _altitudeCalibrationStatus.asStateFlow()
    private val altitudeFlowFrequency = SensorFrequency()
    override val altitudeFlow: Flow<SensorData>
        get() {
            return combine(sensorManager.pressureSensorDataFlow(), locationManager.locationDataFlow(context, 1000)) { p, l ->
                val pressure = p.values[0] * 100f
                var altitude = l.altitude.toFloat()
                with(_altitudeCalibrationStatus.value) {
                    if (l.hasAccuracy() && l.hasVerticalAccuracy() && pressure != 0f && !isCalibrated) {
                        isCalibrated = true
                        pressure0 = pressure
                        altitude0 = altitude
                        verticalAccuracy = l.verticalAccuracyMeters
                        horizontalAccuracy = l.verticalAccuracyMeters
                    }
                    if (altitude0 != 0f && pressure0 != 0f && pressure != 0f) {
                        altitude = calcAltitude(pressure, pressure0, altitude0)
                    }
                }
                SensorData(type = SensorType.Altitude, values = floatArrayOf(altitude), frequency = altitudeFlowFrequency.get())
            }
        }

    /**
     * Climbrate flow
     */
    override val climbRateFlow: Flow<SensorData>
        get() {
            return altitudeFlow.combine(verticalAccelerationFlow) { altitudeData, accelerationData ->
                if (accelerationData.frequency > 0) {
                    kalmanFilter.predict(accelerationData.values[0], 1f / accelerationData.frequency)
                }
                if (altitudeData.frequency > 0) {
                    kalmanFilter.update(altitudeData.values[0])
                }
                SensorData(
                    type = SensorType.Climbrate,
                    values = floatArrayOf(kalmanFilter.climbrate),
                    frequency = accelerationData.frequency
                )
            }
        }

    private fun calcAltitude(pressure: Float, pressure0: Float, altitude0: Float): Float {
        if (altitude0 in Limits.minAltitude..Limits.maxAltitude &&
            pressure0 in Limits.minPressure..Limits.maxPressure &&
            pressure in Limits.minPressure..Limits.maxPressure
        ) {
            val h0 = altitude0.toDouble() // meter
            val ph = pressure.toDouble() // pascal
            val p0 = pressure0.toDouble() // pascal

            /**
             * https://de.wikipedia.org/wiki/Barometrische_Höhenformel
             * Th = 288.15f (15°C)
             * h = (Th/0.0065) * (1.0 - (ph/p0)^(1/5.255))
             **/
            val e = 0.1902949571836346 /* 1 / 5.255 */
            val h = 44330.769 * (1.0 - (ph / p0).pow(e))

            return (h0 + h).toFloat()
        } else {
            return 0f
        }
    }

    private fun <T> Flow<T>.shareSensorData(stopTimeoutMillis: Long = 5000): Flow<T> = shareIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
        replay = 1
    )
}



