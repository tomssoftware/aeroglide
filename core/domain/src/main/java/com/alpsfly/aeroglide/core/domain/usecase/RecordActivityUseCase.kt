package com.alpsfly.aeroglide.core.domain.usecase

import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.model.hardware.SensorData
import com.alpsfly.aeroglide.core.model.hardware.SensorType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class StartRecordActivityUseCase @Inject constructor(
    private val sensorRepository: SensorRepository,
    private val dataRepository: DataRepository,
) {
    private val _recordingStates = mutableMapOf<String, MutableStateFlow<Boolean>>()
    private val recordingJobs = mutableMapOf<String, Job?>()

    fun isRecording(sensorType: SensorType): StateFlow<Boolean> {
        return _recordingStates.getOrPut(sensorType.name) { MutableStateFlow(false) }.asStateFlow()
    }

    fun startRecording(sensorType: SensorType) {
        val isRecordingFlow = _recordingStates.getOrPut(sensorType.name) { MutableStateFlow(false) }
        if (isRecordingFlow.value) return // Prevent multiple starts

        isRecordingFlow.value = true
        recordingJobs[sensorType.name] = CoroutineScope(Dispatchers.IO).launch {
            getSensorFlow(sensorType).collect { sensorData ->
                if (isRecordingFlow.value) {
                    dataRepository.addSensorData(sensorData)
                }
            }
        }
    }

    fun stopRecording(sensorType: SensorType) {
        val isRecordingFlow = _recordingStates[sensorType.name] ?: return
        isRecordingFlow.value = false
        recordingJobs[sensorType.name]?.cancel()
        recordingJobs[sensorType.name] = null
    }

    private fun getSensorFlow(sensorType: SensorType): Flow<SensorData> {
        return when (sensorType) {
            SensorType.Altitude -> sensorRepository.altitudeFlowUi
            SensorType.Climbrate -> sensorRepository.climbrateFlowUi
            else -> throw IllegalArgumentException("Unsupported sensor type: $sensorType")
        }
    }
}

