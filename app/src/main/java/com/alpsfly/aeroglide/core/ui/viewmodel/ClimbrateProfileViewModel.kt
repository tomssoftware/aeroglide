package com.alpsfly.aeroglide.core.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.repository.SensorRepository
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds


@HiltViewModel
class ClimbrateProfileViewModel @Inject constructor(
    sensorRepository: SensorRepository,
) : ViewModel() {

    private val climbrateFlow = sensorRepository.climbRateFlow

    init {
        viewModelScope.launch {
            collectClimbrate()
        }
    }

    val climbrateModelProducer = CartesianChartModelProducer()
    private val climbratePoints = mutableStateListOf<Pair<Int, Float>>()
    @OptIn(FlowPreview::class)
    private suspend fun collectClimbrate() {
        climbrateFlow.sample(1000.milliseconds).collect { climbrate ->
            climbratePoints.add(Pair(climbratePoints.size, climbrate.values[0]))
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