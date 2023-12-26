package com.alpsfly.aeroglide.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.data.repository.SensorRepository
import com.alpsfly.aeroglide.data.util.Limits
import com.alpsfly.aeroglide.data.util.SensorData
import com.alpsfly.aeroglide.data.util.SensorType
import com.alpsfly.aeroglide.data.util.chunked
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SensorViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
) : ViewModel() {
    fun getAcceleration(): Flow<SensorData> {
        return sensorRepository.accelerometerStateFlow
    }

    fun getLocation(): Flow<Location> {
        return sensorRepository.locationStateFlow
    }

    @OptIn(FlowPreview::class)
    fun getAltitude(): Flow<SensorData> {
        return altitudeFlow
            .sample(1000.milliseconds)
    }

    @OptIn(FlowPreview::class)
    fun getPressure(): Flow<SensorData> {
        return getPressure(5)
            .sample(1000.milliseconds)
    }

    private fun getPressure(size: Int): Flow<SensorData> {
        return sensorRepository.pressureStateFlow.chunked(size) { list ->
            val timestamp = list.fold(0L) { sum, item -> sum + item.timestamp } / list.size
            val frequency = list.fold(0f) { sum, item -> sum + item.frequency } / list.size / size
            val pressure = list.fold(0f) { sum, item -> sum + item.values[0] } / list.size.toFloat()
            Timber.v("Send pressure data: $pressure @ $timestamp")
            SensorData(timestamp = timestamp, frequency = frequency, values = floatArrayOf(pressure), type = list[0].type)
        }
    }

    private val altitudeFlow: Flow<SensorData>
        get() {
            var altitude0 = 0f
            var pressure0 = 0f
            return combine(getPressure(5), sensorRepository.locationStateFlow) { p, l ->
                val pressure = p.values[0] * 100f
                var altitude = l.altitude.toFloat()
                if (l.hasAccuracy() && l.hasVerticalAccuracy() && pressure != 0f) {
                    pressure0 = pressure
                    altitude0 = altitude
                }
                if (altitude0 != 0f && pressure0 != 0f && pressure != 0f) {
                    altitude = calcAltitude(pressure, pressure0, altitude0)
                }
                Timber.v("Default altitude: ${l.altitude.toFloat()}, ${p.values[0]} -> $altitude")
                SensorData(timestamp = p.timestamp, values = floatArrayOf(altitude), type = SensorType.Altitude)
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

    private val queue = ArrayDeque<FloatEntry>()
    val chartEntryModelProducer = ChartEntryModelProducer(queue)
    private suspend fun updatePressureGraph() {
        var counter = 1L
        getPressure().collect { sensorData ->
            if (queue.size == 10) {
                queue.removeFirst()
            }
            queue.addLast(FloatEntry(counter.toFloat(), sensorData.values[0]))
            chartEntryModelProducer.setEntries(queue)
            counter++
        }
    }

    private val altitudeQueue = ArrayDeque<FloatEntry>()
    val altitudeChartEntryModelProducer = ChartEntryModelProducer(altitudeQueue)
    private suspend fun updateAltitudeGraph() {
        var counter = 1L
        getAltitude().collect { altitude ->
            if (altitudeQueue.size == 10) {
                altitudeQueue.removeFirst()
            }
            altitudeQueue.addLast(FloatEntry(counter.toFloat(), altitude.values[0]))
            altitudeChartEntryModelProducer.setEntries(altitudeQueue)
            counter++
        }
    }

    init {
        Timber.d("init ${queue.size}")
        viewModelScope.launch {
            updatePressureGraph()
        }
        viewModelScope.launch {
            updateAltitudeGraph()
        }
    }
}
