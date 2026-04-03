package de.tomssoftware.aeroglide.core.data

import android.content.Context
import android.hardware.SensorEventCallback
import android.hardware.SensorManager
import android.location.LocationManager
import de.tomssoftware.aeroglide.core.common.Limits
import de.tomssoftware.aeroglide.core.common.TimeProvider
import de.tomssoftware.aeroglide.core.common.chunked
import de.tomssoftware.aeroglide.core.common.di.ApplicationScope
import de.tomssoftware.aeroglide.core.common.di.SystemTime
import de.tomssoftware.aeroglide.core.common.logDeviation
import de.tomssoftware.aeroglide.core.common.movingAverage
import de.tomssoftware.aeroglide.core.data.util.IKalmanFilter
import de.tomssoftware.aeroglide.core.data.util.KalmanFilter
import de.tomssoftware.aeroglide.core.data.util.Q_ACCELERATION
import de.tomssoftware.aeroglide.core.data.util.R_ALTITUDE
import de.tomssoftware.aeroglide.core.data.util.getVerticalAcceleration
import de.tomssoftware.aeroglide.core.hardware.SensorFrequency
import de.tomssoftware.aeroglide.core.hardware.geoidCorrectionFlow
import de.tomssoftware.aeroglide.core.hardware.linearAccelerationSensorDataFlow
import de.tomssoftware.aeroglide.core.hardware.locationDataFlow
import de.tomssoftware.aeroglide.core.hardware.pressureSensorDataFlow
import de.tomssoftware.aeroglide.core.hardware.rotationVectorSensorDataFlow
import de.tomssoftware.aeroglide.core.model.hardware.Altitude
import de.tomssoftware.aeroglide.core.model.hardware.Climbrate
import de.tomssoftware.aeroglide.core.model.hardware.GlideRatio
import de.tomssoftware.aeroglide.core.model.hardware.Location
import de.tomssoftware.aeroglide.core.model.hardware.Pressure
import de.tomssoftware.aeroglide.core.model.database.SyncState
import de.tomssoftware.aeroglide.core.model.database.TrackPoint
import de.tomssoftware.aeroglide.core.model.database.Calibration
import de.tomssoftware.aeroglide.core.model.hardware.SensorData
import de.tomssoftware.aeroglide.core.model.hardware.SensorType
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.shareIn
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds
import android.location.Location as SensorLocation

interface SensorRepository {

    /** Pressure sensor flow with 200ms delay */
    val pressureDataSource: Flow<SensorData>
    val pressureFlowUi: Flow<Pressure>

    /** Location sensor flow with 1000ms delay */
    val locationDataSource: Flow<SensorLocation>
    val geoidCorrectionDataSource: Flow<Float>
    val locationFlow: Flow<Location>
    val locationFlowUi: Flow<Location>

    val verticalAccelerationFlow: Flow<SensorData>
    val verticalAccelerationFlowUi: Flow<SensorData>

    /** Fused sensor */
    val climbRateFlow: Flow<SensorData>
    val climbrateFlowUi: Flow<Climbrate>

    /** Fused sensor */
    val altitudeFlow: Flow<SensorData>
    val altitudeFlowUi: Flow<Altitude>

    /** Fused sensor */
    val glideRatioFlow: Flow<SensorData>
    val glideRatioFlowUi: Flow<GlideRatio>

    val trackPointFlow: Flow<TrackPoint>

    val calibration: StateFlow<Calibration>
    fun setCalibration(calibration: Calibration)
    fun resetCalibration()

    // ONE MASTER SWITCH
    fun enableSensorListeners()
    fun disableSensorListeners()
}

@Singleton
class SensorRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
    sensorManager: SensorManager,
    fusedLocationProviderClient: FusedLocationProviderClient,
    locationManager: LocationManager,
    @param:SystemTime private val timeProvider: TimeProvider
) : SensorRepository, SensorEventCallback() {

    private val isSensorListenerEnabled = MutableStateFlow(false)
    override fun enableSensorListeners() {
        Timber.i("ENABLE ALL SENSOR LISTENERS")
        isSensorListenerEnabled.value = true
    }

    override fun disableSensorListeners() {
        Timber.i("DISABLE ALL SENSOR LISTENERS")
        isSensorListenerEnabled.value = false
    }

    /**
     * Location state flow
     */
    override val locationDataSource = fusedLocationProviderClient.locationDataFlow(
        context = context,
        enable = isSensorListenerEnabled,
        interval = 1000
    ).shareSensorData()

    @OptIn(FlowPreview::class)
    override val geoidCorrectionDataSource = locationManager.geoidCorrectionFlow(
        context = context,
        enable = isSensorListenerEnabled,
        interval = 1000
    ).shareSensorData().sample(1000.milliseconds)

    // Single shared instance – every subscriber sees the same GPS pipe.
    override val locationFlow: Flow<Location> by lazy {
        combine(locationDataSource, geoidCorrectionDataSource) { l, geoidCorrection ->
            Location(
                timestamp = System.currentTimeMillis(),
                latitude = l.latitude.toFloat(),
                longitude = l.longitude.toFloat(),
                altitude = l.altitude.toFloat() - geoidCorrection,
                bearing = l.bearing,
                speed = l.speed,
                geoidCorrection = geoidCorrection,
                hasHorizontalAccuracy = l.hasAccuracy(),
                horizontalAccuracy = l.accuracy,
                hasVerticalAccuracy = l.hasVerticalAccuracy(),
                verticalAccuracy = l.verticalAccuracyMeters,
                bearingAccuracy = l.bearingAccuracyDegrees,
                speedAccuracy = l.speedAccuracyMetersPerSecond,
                provider = l.provider ?: "unknown"
            )
        }.logDeviation(
            predicate = { Limits.checkSpeed(it.speed) },
            onDeviation = { Timber.w("Speed out of range: ${it.speed}") }
        ).shareSensorData()
    }

    /**
     * pressure state flow
     */
    override val pressureDataSource = sensorManager.pressureSensorDataFlow(
        enable = isSensorListenerEnabled
    ).logDeviation(
        predicate = { Limits.checkPressure(it.values[0]) },
        onDeviation = { Timber.w("Pressure out of range: ${it.values[0]}") }
    )

    /**
     * Linear acceleration shared flow
     */
    private val linearAccelerationDataSource = sensorManager.linearAccelerationSensorDataFlow(
        enable = isSensorListenerEnabled
    )

    /**
     * Rotation vector shared flow
     */
    private val rotationVectorDataSource = sensorManager.rotationVectorSensorDataFlow(
        enable = isSensorListenerEnabled
    )

    /**
     * Vertical acceleration flow
     * Single shared instance – owns the SensorFrequency counter for this pipe.
     */
    override val verticalAccelerationFlow: Flow<SensorData> by lazy {
        val sensorFrequency = SensorFrequency()
        combine(linearAccelerationDataSource, rotationVectorDataSource) { a, r ->
            getVerticalAcceleration(a, r)
        }.map {
            SensorData(
                type = SensorType.VerticalAcceleration,
                timestamp = timeProvider.currentTimeMillis(),
                frequency = sensorFrequency.inc(),
                values = floatArrayOf(it)
            )
        }.logDeviation(
            predicate = { Limits.checkVerticalAcceleration(it.values[0]) },
            onDeviation = { Timber.w("Vertical acceleration out of range: ${it.values[0]}") }
        ).shareSensorData()
    }

    override val verticalAccelerationFlowUi: Flow<SensorData> by lazy {
        val sensorFrequency = SensorFrequency()
        verticalAccelerationFlow
            .map { d -> d.values[0] }
            .chunked(1000.milliseconds)
            .map { l ->
                SensorData(
                    type = SensorType.VerticalAcceleration,
                    timestamp = System.currentTimeMillis(),
                    frequency = sensorFrequency.inc(),
                    values = floatArrayOf(l.average().toFloat())
                )
            }
    }

    override val pressureFlowUi: Flow<Pressure> by lazy {
        val sensorFrequency = SensorFrequency()
        pressureDataSource
            .map { d -> d.values[0] }
            .chunked(1000.milliseconds)
            .map { l ->
                Pressure(
                    timestamp = System.currentTimeMillis(),
                    frequency = sensorFrequency.inc(),
                    pressure = l.average().toFloat()
                )
            }
    }

    /**
     * Altitude flow
     * Single shared instance so that [climbRateFlow] and [altitudeFlowUi] observe
     * the same barometric altitude pipe.
     */
    override val altitudeFlow: Flow<SensorData> by lazy {
        val sensorFrequency = SensorFrequency()
        combine(pressureDataSource, locationFlow) { p, l ->
            val pressure = p.values[0]
            var altitude = l.altitude
            if (calibration.value.isCalibrated) {
                altitude = calcAltitude(
                    pressure,
                    calibration.value.pressure0,
                    calibration.value.altitude0
                )
            }
            SensorData(
                type = SensorType.Altitude,
                timestamp = System.currentTimeMillis(),
                frequency = sensorFrequency.inc(),
                values = floatArrayOf(altitude)
            )
        }.logDeviation(
            predicate = { Limits.checkAltitude(it.values[0]) },
            onDeviation = { Timber.w("Altitude out of range: ${it.values[0]}") }
        ).shareSensorData()
    }

    // Shared so that trackPointFlow (DB) and the UI chart observe the identical
    // 1-second averaged altitude – no divergence between live view and history.
    override val altitudeFlowUi: Flow<Altitude> by lazy {
        val sensorFrequency = SensorFrequency()
        altitudeFlow
            .map { d -> d.values[0] }
            .chunked(1000.milliseconds)
            .map { l ->
                Altitude(
                    timestamp = System.currentTimeMillis(),
                    frequency = sensorFrequency.inc(),
                    altitude = l.average().toFloat()
                )
            }
            .shareSensorData()
    }

    /**
     * Climbrate flow – SSOT for all climbrate consumers.
     *
     * Owns the single [KalmanFilter] instance that fuses barometric altitude and
     * vertical acceleration. Converting from a `get()` property to a `val` ensures
     * that [trackPointFlow] (DB storage) and [climbrateFlowUi] (live chart) both
     * observe the same filtered signal, eliminating live-vs-history divergence.
     */
    override val climbRateFlow: Flow<SensorData> by lazy {
        val sensorFrequency = SensorFrequency()
        val kalmanFilter: IKalmanFilter = KalmanFilter(Q_ACCELERATION, R_ALTITUDE)
        var lastAltitude = 0L
        var lastVerticalAcceleration = 0L
        var isKalmanFilterConfigured = false
        combine(altitudeFlow, verticalAccelerationFlow) { a, v ->
            // Schritt 1 – Prädiktionsschritt (predict VOR update, korrekter Kalman-Zyklus)
            if (v.timestamp != lastVerticalAcceleration) {
                if (isKalmanFilterConfigured && sensorFrequency.get() > 0) {
                    kalmanFilter.predict(v.values[0], 1f / sensorFrequency.get())
                }
                lastVerticalAcceleration = v.timestamp
                sensorFrequency.inc()
            }
            // Schritt 2 – Korrekturschritt
            if (a.timestamp != lastAltitude) {
                if (lastAltitude == 0L) {
                    kalmanFilter.configure(Q_ACCELERATION, R_ALTITUDE, a.values[0])
                    isKalmanFilterConfigured = true
                }
                kalmanFilter.update(a.values[0])
                lastAltitude = a.timestamp
            }
            kalmanFilter.climbrate
        }.movingAverage(20)
            .map { a ->
                SensorData(
                    type = SensorType.Climbrate,
                    timestamp = System.currentTimeMillis(),
                    frequency = sensorFrequency.get(),
                    values = floatArrayOf(a)
                )
            }.logDeviation(
                predicate = { Limits.checkClimbrate(it.values[0]) },
                onDeviation = { Timber.w("Climbrate out of range: ${it.values[0]}") }
            ).shareSensorData()
    }

    // Shared so that trackPointFlow (DB) and the live chart observe the identical
    // 1-second averaged climbrate – no divergence between live view and history.
    override val climbrateFlowUi: Flow<Climbrate> by lazy {
        val sensorFrequency = SensorFrequency()
        climbRateFlow
            .map { s -> s.values[0] }
            .chunked(1000.milliseconds)
            .map { l ->
                Climbrate(
                    timestamp = System.currentTimeMillis(),
                    frequency = sensorFrequency.inc(),
                    climbrate = l.average().toFloat()
                )
            }
            .shareSensorData()
    }

    @OptIn(FlowPreview::class)
    override val locationFlowUi: Flow<Location> = locationFlow.sample(1000.milliseconds)

    // Single shared instance – glideRatioFlowUi and trackPointFlow see the same pipe.
    override val glideRatioFlow: Flow<SensorData> by lazy {
        val sensorFrequency = SensorFrequency()
        combine(locationDataSource, climbRateFlow) { l, c ->
            val glideRatio = when (c.values[0] <= -0.3) {
                true -> l.speed / c.values[0]
                false -> 0f
            }
            SensorData(
                type = SensorType.GlideRatio,
                timestamp = System.currentTimeMillis(),
                frequency = sensorFrequency.inc(),
                values = floatArrayOf(glideRatio)
            )
        }
    }

    override val glideRatioFlowUi: Flow<GlideRatio> by lazy {
        val sensorFrequency = SensorFrequency()
        glideRatioFlow
            .map { s -> s.values[0] }
            .chunked(1000.milliseconds)
            .map { l ->
                GlideRatio(
                    timestamp = System.currentTimeMillis(),
                    frequency = sensorFrequency.inc(),
                    glideRatio = l.average().toFloat()
                )
            }
    }

    // SSOT: both the live chart and the database use the same shared 1-second averaged
    // flows (altitudeFlowUi, climbrateFlowUi), so history always matches the live view.
    override val trackPointFlow: Flow<TrackPoint> = combine(
        locationFlow,
        altitudeFlowUi,
        pressureDataSource,
        climbrateFlowUi,
        glideRatioFlow
    ) { location, altitude, pressure, climbrate, glideRatio ->
        TrackPoint(
            timestamp = location.timestamp, // Wir nutzen den GPS-Zeitstempel als Anker
            latitude = location.latitude,
            longitude = location.longitude,
            gpsAltitude = location.altitude,
            bearing = location.bearing,
            speed = location.speed,
            geoidCorrection = location.geoidCorrection,
            hasHorizontalAccuracy = location.hasHorizontalAccuracy,
            horizontalAccuracy = location.horizontalAccuracy,
            hasVerticalAccuracy = location.hasVerticalAccuracy,
            verticalAccuracy = location.verticalAccuracy,
            bearingAccuracy = location.bearingAccuracy,
            speedAccuracy = location.speedAccuracy,
            provider = location.provider,

            // 1-second averaged values – identical to what the live charts display
            altitude = altitude.altitude,
            pressure = pressure.values[0],
            climbrate = climbrate.climbrate,
            glideRatio = glideRatio.values[0],

            // Initialer Sync-Status
            syncState = SyncState.LOCAL
        )
    }.shareSensorData() // Wichtig: Damit nur eine Pipe für alle Observer offen ist

    private fun calcAltitude(pressure: Float, pressure0: Float, altitude0: Float): Float {
        val h0 = altitude0.toDouble() // meter
        val ph = pressure.toDouble() * 100f // pascal
        val p0 = pressure0.toDouble() * 100f // pascal

        /**
         * https://de.wikipedia.org/wiki/Barometrische_Höhenformel
         * Th = 288.15f (15°C)
         * h = (Th/0.0065) * (1.0 - (ph/p0)^(1/5.255))
         **/
        val e = 0.1902949571836346 /* 1 / 5.255 */
        val h = 44330.769 * (1.0 - (ph / p0).pow(e))

        return (h0 + h).toFloat()
    }

    private val _calibration = MutableStateFlow(Calibration())
    override val calibration = _calibration.asStateFlow()
    override fun setCalibration(calibration: Calibration) {
        _calibration.value = calibration
    }

    override fun resetCalibration() {
        _calibration.value = Calibration()
    }

    private fun <T> Flow<T>.shareSensorData(stopTimeoutMillis: Long = 5000): Flow<T> = shareIn(
        scope = applicationScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
        replay = 1
    )
}
