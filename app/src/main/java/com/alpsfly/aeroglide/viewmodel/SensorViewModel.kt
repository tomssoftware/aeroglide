package com.alpsfly.aeroglide.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.data.repository.LocationRepository
import com.alpsfly.aeroglide.data.repository.SensorRepository
import com.alpsfly.aeroglide.data.util.SensorValues
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SensorViewModel @Inject constructor(
    sensorRepository: SensorRepository,
    locationRepository: LocationRepository,
) : ViewModel() {
    private val accelDataSource = sensorRepository.accelDataSource.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    private val sharedPressureSensorDataFlow = sensorRepository.pressureDataFlow.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    private val locationDataFlow = locationRepository.locationDataSource.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun getAccelSenorData(): SharedFlow<SensorValues> {
        return accelDataSource.shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )
    }

    fun getPressureSenorData(): SharedFlow<SensorValues> {
        return sharedPressureSensorDataFlow.shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )
    }

    fun getLocationData(): SharedFlow<Location> {
        return locationDataFlow.shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed( 5000 )
        )
    }

    private val queue = ArrayDeque<FloatEntry>()
    val chartEntryModelProducer = ChartEntryModelProducer(queue)

    private suspend fun updatePressureGraph() {
        val sensorValues = mutableListOf<SensorValues>()
        var startTime = 0L
        var counter = 1L

        sharedPressureSensorDataFlow.collect { values ->
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - startTime
            sensorValues.add(values)
            if (elapsedTime >= 10000) {
                val at = sensorValues.fold(0L) { sum, item -> sum + item.timestamp } / sensorValues.size
                val ax = sensorValues.fold(0f) { sum, item -> sum + item.x } / sensorValues.size.toFloat()
                val ay = sensorValues.fold(0f) { sum, item -> sum + item.y } / sensorValues.size.toFloat()
                val az = sensorValues.fold(0f) { sum, item -> sum + item.z } / sensorValues.size.toFloat()

                if (queue.size == 10) {
                    queue.removeFirst()
                }

                queue.addLast(FloatEntry(counter.toFloat(), ax))
                chartEntryModelProducer.setEntries(queue)

                sensorValues.clear()
                startTime = currentTime
                counter++
            }
        }
    }

    init {
        viewModelScope.launch {
            updatePressureGraph()
        }
    }
}