package com.alpsfly.aeroglide.feature.devicestatus.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceStatusViewModel @Inject constructor(
    sensorRepository: SensorRepository,
) : ViewModel() {

    private val altitudeFlow = sensorRepository.altitudeFlowUi
    private val climbrateFlow = sensorRepository.climbrateFlowUi
    private val pressureFlow = sensorRepository.pressureFlowUi

    val pressureModelProducer = CartesianChartModelProducer()
    private val pressurePoints = mutableStateListOf<Pair<Int, Float>>()

    val altitudeModelProducer = CartesianChartModelProducer()
    private val altitudePoints = mutableStateListOf<Pair<Int, Float>>()

    val climbrateModelProducer = CartesianChartModelProducer()
    private val climbratePoints = mutableStateListOf<Pair<Int, Float>>()

    val altitudeCalibrationStatus = sensorRepository.altitudeCalibrationStatus

    init {
        viewModelScope.launch {
            collectPressure()
        }
        viewModelScope.launch {
            collectAltitude()
        }
        viewModelScope.launch {
            collectClimbrate()
        }
    }

    private suspend fun collectPressure() {
        pressureFlow.collect { pressure ->
            pressurePoints.add(Pair(pressurePoints.size, pressure.pressure))
            pressureModelProducer.runTransaction {
                lineSeries {
                    series(
                        x = pressurePoints.map { it.first },
                        y = pressurePoints.map { it.second }
                    )
                }
            }
        }
    }

    private suspend fun collectAltitude() {
        altitudeFlow.collect { altitude ->
            altitudePoints.add(Pair(altitudePoints.size, altitude.altitude))
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

    private suspend fun collectClimbrate() {
        climbrateFlow.collect { climbrate ->
            climbratePoints.add(Pair(climbratePoints.size, climbrate.climbrate))
            climbrateModelProducer.runTransaction {
                lineSeries {
                    series(
                        x = climbratePoints.map { it.first },
                        y = climbratePoints.map { it.second }
                    )
                }
            }
        }
    }
}