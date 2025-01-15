package com.alpsfly.aeroglide.core.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds


@HiltViewModel
class AltitudeProfileViewModel @Inject constructor(
    sensorRepository: SensorRepository,
    dataRepository: DataRepository,
) : ViewModel() {

    private val altitudeFlow = sensorRepository.altitudeFlowUi
    private val altitude = dataRepository.altitude

    init {
        viewModelScope.launch {
            collectAltitude()
        }
    }

    val altitudeModelProducer = CartesianChartModelProducer()
    private val altitudePoints = mutableStateListOf<Pair<Int, Float>>()
    private suspend fun collectAltitude() {
        altitude.collect { altitude ->
            altitude.forEach { altitudex ->
                altitudePoints.add(Pair(altitudePoints.size, altitudex.altitude))
                altitudeModelProducer.runTransaction {
                    lineSeries {
                        series(
                            x = altitudePoints.map { it.first },
                            y = altitudePoints.map { it.second }
                        )
                    }
                }
            }
        }
    }
}