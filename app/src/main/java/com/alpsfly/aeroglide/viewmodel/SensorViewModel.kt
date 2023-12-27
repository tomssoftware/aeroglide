package com.alpsfly.aeroglide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.data.repository.SensorRepository
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SensorViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
) : ViewModel() {
    @OptIn(FlowPreview::class)
    val acceleration = sensorRepository.accelerometerDataSource.sample(1000.milliseconds)

    @OptIn(FlowPreview::class)
    val pressure = sensorRepository.pressureDataSource.sample(1000.milliseconds)

    // location
    val location = sensorRepository.locationDataSource

    @OptIn(FlowPreview::class)
    val verticalAcceleration = sensorRepository.verticalAccelerationFlow.sample(1000.milliseconds)

    @OptIn(FlowPreview::class)
    val altitude = sensorRepository.altitudeFlow.sample(1000.milliseconds)

    @OptIn(FlowPreview::class)
    val climbrate = sensorRepository.climbRateFlow.sample(1000.milliseconds)

    // Update charts
    private val pressureQueue = ArrayDeque<FloatEntry>()
    val pressureChartEntryModelProducer = ChartEntryModelProducer(pressureQueue)
    private suspend fun updatePressureGraph() {
        var counter = 1L
        pressure.collect { sensorData ->
            if (pressureQueue.size == 10) {
                pressureQueue.removeFirst()
            }
            pressureQueue.addLast(FloatEntry(counter.toFloat(), sensorData.values[0]))
            pressureChartEntryModelProducer.setEntries(pressureQueue)
            counter++
        }
    }

    private val altitudeQueue = ArrayDeque<FloatEntry>()
    val altitudeChartEntryModelProducer = ChartEntryModelProducer(altitudeQueue)
    private suspend fun updateAltitudeGraph() {
        var counter = 1L
        altitude.collect { altitude ->
            if (altitudeQueue.size == 10) {
                altitudeQueue.removeFirst()
            }
            altitudeQueue.addLast(FloatEntry(counter.toFloat(), altitude.values[0]))
            altitudeChartEntryModelProducer.setEntries(altitudeQueue)
            counter++
        }
    }

    private val climbrateQueue = ArrayDeque<FloatEntry>()
    val climbrateChartEntryModelProducer = ChartEntryModelProducer(climbrateQueue)
    private suspend fun updateClimbrateGraph() {
        var counter = 1L
        climbrate.collect { climbrate ->
            if (climbrateQueue.size == 10) {
                climbrateQueue.removeFirst()
            }
            climbrateQueue.addLast(FloatEntry(counter.toFloat(), climbrate.values[0]))
            climbrateChartEntryModelProducer.setEntries(climbrateQueue)
            counter++
        }
    }

    init {
        viewModelScope.launch {
            updatePressureGraph()
        }
        viewModelScope.launch {
            updateAltitudeGraph()
        }
        viewModelScope.launch {
            updateClimbrateGraph()
        }
    }
}
