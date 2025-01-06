package com.alpsfly.aeroglide.feature.devicestatus.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.common.units.LocalUnit
import com.alpsfly.aeroglide.core.common.units.UnitConverter
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
class VicoChartViewModel @Inject constructor(
    sensorRepository: SensorRepository,
) : ViewModel() {

    private val altitudeFlow = sensorRepository.altitudeFlowUi
    private val climbrateFlow = sensorRepository.climbrateFlowUi
    private val pressureFlow = sensorRepository.pressureFlowUi

    init {
        viewModelScope.launch {
            collectAltitude()
        }
        viewModelScope.launch {
            collectClimbrate()
        }
        viewModelScope.launch {
            collectPressure()
        }
    }

    val altitudeCalibrationStatus = sensorRepository.altitudeCalibrationStatus

    val pressureModelProducer = CartesianChartModelProducer()
    private val pressurePoints = mutableStateListOf<Pair<Int, Float>>()

    private suspend fun collectPressure() {
        pressureFlow.collect { pressure ->
            pressurePoints.add(Pair(pressurePoints.size, pressure.values[0]))
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

    val altitudeModelProducer = CartesianChartModelProducer()
    private val altitudePoints = mutableStateListOf<Pair<Int, Float>>()

    private suspend fun collectAltitude() {
        altitudeFlow.collect { altitude ->
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

    val climbrateModelProducer = CartesianChartModelProducer()
    private val climbratePoints = mutableStateListOf<Pair<Int, Float>>()

    private suspend fun collectClimbrate() {
        climbrateFlow.collect { climbrate ->
            climbratePoints.add(Pair(climbratePoints.size, climbrate.values[0]))
            climbrateModelProducer.runTransaction {
                lineSeries {
                    series(
                        x = climbratePoints.map { it.first },
                        y = climbratePoints.map { it.second } //{ LocalUnit.of(it.second, UnitConverter.Unit.MS).toValue() }
                    )
                }
            }
        }
    }
}