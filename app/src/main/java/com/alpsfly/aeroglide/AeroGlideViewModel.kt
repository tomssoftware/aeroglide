package com.alpsfly.aeroglide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.domain.usecase.StartRecordActivityUseCase
import com.alpsfly.aeroglide.core.model.hardware.SensorType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AeroGlideViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
    private val dataRepository: DataRepository,
    private val recordSensorDataUseCase: StartRecordActivityUseCase,
) : ViewModel() {

    val isRecording: StateFlow<Boolean> = recordSensorDataUseCase.isRecording(SensorType.Altitude)

    fun startRecording() {
        recordSensorDataUseCase.startRecording(sensorType = SensorType.Altitude)
        recordSensorDataUseCase.startRecording(sensorType = SensorType.Climbrate)
    }

    fun stopRecording() {
        recordSensorDataUseCase.stopRecording(sensorType = SensorType.Altitude)
        recordSensorDataUseCase.stopRecording(sensorType = SensorType.Climbrate)
    }
}