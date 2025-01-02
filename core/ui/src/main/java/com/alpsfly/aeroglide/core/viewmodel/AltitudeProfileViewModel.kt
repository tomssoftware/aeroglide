package com.alpsfly.aeroglide.core.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
) : ViewModel() {

    private val altitudeFlow = sensorRepository.altitudeFlowUi

    init {
        viewModelScope.launch {
            collectAltitude()
        }
    }

    val altitudeModelProducer = CartesianChartModelProducer()
    private val altitudePoints = mutableStateListOf<Pair<Int, Float>>()
    @OptIn(FlowPreview::class)
    private suspend fun collectAltitude() {
        altitudeFlow.sample(1000.milliseconds).collect { altitude ->
            altitudePoints.add(Pair(altitudePoints.size, altitude.values[0]))
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