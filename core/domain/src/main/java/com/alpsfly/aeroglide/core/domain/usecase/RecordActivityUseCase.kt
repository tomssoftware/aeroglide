package com.alpsfly.aeroglide.core.domain.usecase

import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.model.database.Activity
import com.alpsfly.aeroglide.core.model.database.Altitude
import com.alpsfly.aeroglide.core.model.database.Climbrate
import com.alpsfly.aeroglide.core.model.database.Pressure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class StartRecordActivityUseCase @Inject constructor(
    private val sensorRepository: SensorRepository,
    private val dataRepository: DataRepository,
) {

    enum class RecordingSensorType {
        ALTITUDE, CLIMBRATE, PRESSURE
    }

    private val recordingJobs = mutableMapOf<RecordingSensorType, Job>()

    private val sensorFlows = listOf(
        RecordingSensorType.ALTITUDE to sensorRepository.altitudeFlowUi,
        RecordingSensorType.CLIMBRATE to sensorRepository.climbrateFlowUi,
        RecordingSensorType.PRESSURE to sensorRepository.pressureFlowUi,
    )

    private var activity = Activity()
    fun startRecording() {
        CoroutineScope(Dispatchers.IO).launch {
            activity = insertActivity()
        }
        startRecordSensorData()
    }

    fun stopRecording() {
        stopRecordSensorData()
    }

    private fun startRecordSensorData() {
        sensorFlows.forEach { (type, flow) ->
            val job = CoroutineScope(Dispatchers.IO).launch {

                flow.collect { data ->
                    when (type) {
                        RecordingSensorType.ALTITUDE -> {
                            dataRepository.addAltitude(data as Altitude)
                            if (data.altitude > activity.maxAltitude) {
                                activity.maxAltitude = data.altitude
                                updateActivity(activity)
                            }
                            if (data.altitude < activity.minAltitude) {
                                activity.minAltitude = data.altitude
                                updateActivity(activity)
                            }
                            activity.end = System.currentTimeMillis()
                            updateActivity(activity)
                        }

                        RecordingSensorType.CLIMBRATE -> {
                            dataRepository.addClimbrate(data as Climbrate)
                            if (data.climbrate > activity.maxClimbrate) {
                                activity.maxClimbrate = data.climbrate
                                updateActivity(activity)
                            }
                            if (data.climbrate < activity.minClimbrate) {
                                activity.minClimbrate = data.climbrate
                                updateActivity(activity)
                            }
                        }

                        RecordingSensorType.PRESSURE -> {
                            dataRepository.addPressure(data as Pressure)
                            if (data.pressure > activity.maxPressure) {
                                activity.maxPressure = data.pressure
                                updateActivity(activity)
                            }
                            if (data.pressure < activity.minPressure) {
                                activity.minPressure = data.pressure
                                updateActivity(activity)
                            }
                        }
                    }
                }
            }
            recordingJobs[type] = job
        }
    }

    private fun stopRecordSensorData() {
        recordingJobs.forEach { (_, job) -> job.cancel() }
        recordingJobs.clear()
    }

    private suspend fun insertActivity(): Activity {
        val activity = Activity(
            activityId = System.currentTimeMillis(),
            userId = "Thomas",
            begin = System.currentTimeMillis(),
        )
        dataRepository.addActivity(activity)
        return activity
    }

    private suspend fun updateActivity(activity: Activity) {
        dataRepository.updateActivity(activity)
    }
}

