package com.alpsfly.aeroglide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.domain.usecase.CalibrationUseCase
import com.alpsfly.aeroglide.core.domain.usecase.RecordActivityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AeroGlideViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val dataRepository: DataRepository,
    private val sensorRepository: SensorRepository,
    private val recordSensorDataUseCase: RecordActivityUseCase,
    private val calibrationUseCase: CalibrationUseCase,
) : ViewModel() {

    val isRecording = appRepository.isRecording
    private var activityId = 0L
    fun startRecording() {
        activityId = System.currentTimeMillis()
        appRepository.startRecording(activityId)
    }
    fun stopRecording() {
        activityId = 0L
        appRepository.stopRecording()
    }

    fun enableSensorListener() {
        sensorRepository.enableSensorListener()
    }

    fun disableSensorListener() {
        sensorRepository.disableSensorListener()
    }

    private val calibrationJob: Job = viewModelScope.launch(Dispatchers.IO) {
        calibrationUseCase.invoke().collect { calibration ->
            sensorRepository.setCalibration(calibration)
        }
    }
    private val recordingJob: Job = viewModelScope.launch(Dispatchers.IO) {
        isRecording.collect {
            if (it) {
                recordSensorDataUseCase.startRecording(activityId)
            } else {
                recordSensorDataUseCase.stopRecording()
            }
        }
    }

    fun restartCalibration() {
        Timber.i("RESTART CALIBRATION")
        // todo: check if not already running
        viewModelScope.launch(Dispatchers.IO) {
            calibrationUseCase.invoke().collect { calibration ->
                sensorRepository.setCalibration(calibration)
                // todo: store calibration in database
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.i("CLEARED VIEWMODEL")

        // todo: this lines seems not necessary, check
        // sensorRepository.setCalibration(Calibration())
        // calibrationJob.cancel()
        // recordingJob.cancel()
    }
}