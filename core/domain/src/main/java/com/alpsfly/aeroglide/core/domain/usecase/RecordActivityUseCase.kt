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
    private val recordingJobs = mutableMapOf<String, Job?>(
        "Altitude" to null,
        "Climbrate" to null,
        "Pressure" to null,
    )

    fun isRecording(sensorType: SensorType): StateFlow<Boolean> {
        return _recordingStates.getOrPut(sensorType.name) { MutableStateFlow(false) }.asStateFlow()
    }

    fun startRecording(sensorType: SensorType) {
        val isRecordingFlow = _recordingStates.getOrPut(sensorType.name) { MutableStateFlow(false) }
        if (isRecordingFlow.value) return // Prevent multiple starts

        isRecordingFlow.value = true

        recordingJobs.forEach {
            when (it.key) {
                "Altitude" -> {
                    val job = CoroutineScope(Dispatchers.IO).launch {
                        sensorRepository.altitudeFlowUi.collect { altitude ->
                            dataRepository.addAltitude(altitude)
                        }
                    }
                }

                "Climbrate" -> {
                    val job = CoroutineScope(Dispatchers.IO).launch {
                        sensorRepository.climbrateFlowUi.collect { climbrate ->
                            dataRepository.addClimbrate(climbrate)
                        }
                    }
                }

                "Pressure" -> {
                    val job = CoroutineScope(Dispatchers.IO).launch {
                        sensorRepository.pressureFlowUi.collect { pressure ->
                            dataRepository.addPressure(pressure)
                        }
                    }
                }
            }
        }
    }

    fun stopRecording(sensorType: SensorType) {
        val isRecordingFlow = _recordingStates[sensorType.name] ?: return
        isRecordingFlow.value = false

        recordingJobs.forEach {
            it.value?.cancel()
            //it.value = null
        }
    }
}

