package com.alpsfly.aeroglide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.data.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SensorViewModel @Inject constructor(
    private val sensorRepository: SensorRepository
) : ViewModel() {
    val accelDataSource = sensorRepository.accelDataSource.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    val avgPressureFlow = sensorRepository.avgPressureFlow.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    val avgPressureStateFlow = MutableStateFlow(emptyList<Float>())
    private fun getPressureList() {
        viewModelScope.launch { Dispatchers.IO
            sensorRepository.avgPressureFlow.collect {value ->
                val queue = avgPressureStateFlow.value
                if (queue.size < 10) {
                    avgPressureStateFlow.value = queue + value
                } else {
                    avgPressureStateFlow.value = queue.drop(1) + value
                }
            }
        }
    }

    init {
        getPressureList()
    }
}