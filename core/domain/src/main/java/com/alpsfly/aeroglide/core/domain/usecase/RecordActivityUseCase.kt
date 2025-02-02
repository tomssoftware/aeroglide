package com.alpsfly.aeroglide.core.domain.usecase

import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.model.database.Activity
import com.alpsfly.aeroglide.core.model.hardware.SensorType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class StartRecordActivityUseCase @Inject constructor(
    private val appRepository: AppRepository,
    private val sensorRepository: SensorRepository,
    private val dataRepository: DataRepository,
) {
    private val recordingJobs = mutableMapOf<String, Job?>(
        "Altitude" to null,
        "Climbrate" to null,
        "Pressure" to null,
    )

    fun startRecording() {
        if (appRepository.isRecording.value)
            return

        recordingJobs.forEach {
            when (it.key) {
                "Activity" -> {
                    val job = CoroutineScope(Dispatchers.IO).launch {
                        addActivity()
                    }
                    job.cancel()
                }

                "Altitude" -> {
                    val job = CoroutineScope(Dispatchers.IO).launch {
                        sensorRepository.altitudeFlowUi.collect { altitude ->
                            dataRepository.addAltitude(altitude)
                        }
                    }
                    recordingJobs["Altitude"] = job
                }

                "Climbrate" -> {
                    val job = CoroutineScope(Dispatchers.IO).launch {
                        sensorRepository.climbrateFlowUi.collect { climbrate ->
                            dataRepository.addClimbrate(climbrate)
                        }
                    }
                    recordingJobs["Climbrate"] = job
                }

                "Pressure" -> {
                    val job = CoroutineScope(Dispatchers.IO).launch {
                        sensorRepository.pressureFlowUi.collect { pressure ->
                            dataRepository.addPressure(pressure)
                        }
                    }
                    recordingJobs["Pressure"] = job
                }
            }
        }
    }

    fun stopRecording() {
        if (!appRepository.isRecording.value)
            return

        recordingJobs.forEach {
            if (it.key == "Activity") {
                val job = CoroutineScope(Dispatchers.IO).launch {
                    updateActivity()
                }
                job.cancel()
            }
            it.value?.cancel()
        }
        recordingJobs["Altitude"] = null
        recordingJobs["Climbrate"] = null
        recordingJobs["Pressure"] = null
    }

    private suspend fun addActivity() {
        dataRepository.addActivity(
            Activity(
                trackId = System.currentTimeMillis(),
                userId = "Thomas",
                begin = System.currentTimeMillis(),
            )
        )
    }

    private suspend fun updateActivity() {
        dataRepository.updateActivity(
            Activity(
                maxClimbrate = 33f,
                end = System.currentTimeMillis()
            )
        )
    }
}

