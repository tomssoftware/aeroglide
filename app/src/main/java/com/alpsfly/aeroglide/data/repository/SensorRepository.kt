package com.alpsfly.aeroglide.data.repository

import android.content.Context
import android.hardware.SensorEventCallback
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import com.alpsfly.aeroglide.data.util.IKalmanFilter
import com.alpsfly.aeroglide.data.util.KalmanFilter
import com.alpsfly.aeroglide.data.util.Limits
import com.alpsfly.aeroglide.data.util.Q_ACCELERATION
import com.alpsfly.aeroglide.data.util.R_ALTITUDE
import com.alpsfly.aeroglide.data.util.SensorData
import com.alpsfly.aeroglide.data.util.SensorType
import com.alpsfly.aeroglide.data.util.accelerometerSensorDataFlow
import com.alpsfly.aeroglide.data.util.chunked
import com.alpsfly.aeroglide.data.util.getVerticalAcceleration
import com.alpsfly.aeroglide.data.util.linearAccelerationSensorDataFlow
import com.alpsfly.aeroglide.data.util.locationDataFlow
import com.alpsfly.aeroglide.data.util.pressureSensorDataFlow
import com.alpsfly.aeroglide.data.util.rotationVectorSensorDataFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import timber.log.Timber
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
}

@Singleton
class SensorRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorRepository, SensorEventCallback() {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val kalmanFilter: IKalmanFilter = KalmanFilter(Q_ACCELERATION, R_ALTITUDE)

    init {
        Timber.v("SensorRepositoryImpl init")
    }

    /**
     * acceleration state flow
     */
    override val accelerometerDataSource = sensorManager.accelerometerSensorDataFlow().shareIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(5000),
        replay = 1
    )

    /**
     * Linear acceleration state flow
     */
    override val linearAccelerationDataSource = sensorManager.linearAccelerationSensorDataFlow().shareIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(5000),
        replay = 1
    )

    /**
     * pressure state flow
     */
    override val pressureDataSource = sensorManager.pressureSensorDataFlow().shareIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(5000),
        replay = 1
    )

    /**
     * Rotation vector state flow
     */
    override val rotationVectorDataSource = sensorManager.rotationVectorSensorDataFlow().shareIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(5000),
        replay = 1
    )

    /**
     * Location state flow
     */
    override val locationDataSource = locationManager.locationDataFlow(context, 1000).shareIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(5000),
        replay = 1
    )

    /**
     * Vertical acceleration flow
     */
    override val verticalAccelerationFlow: Flow<SensorData>
        get() {
            return combine(sensorManager.linearAccelerationSensorDataFlow(), sensorManager.rotationVectorSensorDataFlow()) { a, r ->
                SensorData(
                    type = SensorType.VerticalAcceleration,
                    frequency = (a.frequency + r.frequency) / 2f,
                    values = floatArrayOf(getVerticalAcceleration(a, r))
                )
            }.shareIn(
                scope = repositoryScope,
                started = SharingStarted.WhileSubscribed(5000),
                replay = 1
            )
        }

    private fun getPressure(size: Int): Flow<SensorData> {
        return sensorManager.pressureSensorDataFlow().chunked(size) { list ->
            val timestamp = list.fold(0L) { sum, item -> sum + item.timestamp } / list.size
            val frequency = list.fold(0f) { sum, item -> sum + item.frequency } / list.size / size
            val pressure = list.fold(0f) { sum, item -> sum + item.values[0] } / list.size.toFloat()
            SensorData(timestamp = timestamp, frequency = frequency, values = floatArrayOf(pressure), type = list[0].type)
        }
    }

    /**
     * Altitude flow
     */
    override val altitudeFlow: Flow<SensorData>
        get() {
            var altitude0 = 0f
            var pressure0 = 0f
            return combine(sensorManager.pressureSensorDataFlow(), locationManager.locationDataFlow(context, 1000)) { p, l ->
                val pressure = p.values[0] * 100f
                var altitude = l.altitude.toFloat()
                if (l.hasAccuracy() && l.hasVerticalAccuracy() && pressure != 0f) {
                    pressure0 = pressure
                    altitude0 = altitude
                }
                if (altitude0 != 0f && pressure0 != 0f && pressure != 0f) {
                    altitude = calcAltitude(pressure, pressure0, altitude0)
                }
                SensorData(
                    type = SensorType.Altitude,
                    values = floatArrayOf(altitude),
                    frequency = (p.frequency + 1) / 2
                )
            }.shareIn(
                scope = repositoryScope,
                started = SharingStarted.WhileSubscribed(5000),
                replay = 1
            )
        }

    /**
     * Climbrate flow
     */
    override val climbRateFlow: Flow<SensorData>
        get() {
            return merge(altitudeFlow, verticalAccelerationFlow)
                .onEach {
                    if (it.type == SensorType.Altitude) {
                        kalmanFilter.update(it.values[0])
                    }
                    if (it.type == SensorType.VerticalAcceleration) {
                        if (it.frequency > 0) {
                            kalmanFilter.predict(it.values[0], 1f / it.frequency)
                        }
                    }
                }.map {
                    Timber.v("cr: ${kalmanFilter.climbrate}")
                    SensorData(type = SensorType.Climbrate, values = floatArrayOf(kalmanFilter.climbrate), frequency = it.frequency)
                } // already a shared flow
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
}



