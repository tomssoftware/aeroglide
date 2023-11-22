package com.alpsfly.aeroglide.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.data.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

@HiltViewModel
class SensorViewModel @Inject constructor(
    sensorRepository: SensorRepository
) : ViewModel() {
    val accelDataSource = sensorRepository.accelDataSource.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )
}