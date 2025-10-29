package com.alpsfly.aeroglide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.AppState
import com.alpsfly.aeroglide.core.domain.usecase.CalibrationUseCase
import com.alpsfly.aeroglide.core.domain.usecase.RecordActivityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AeroGlideViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val recordSensorDataUseCase: RecordActivityUseCase,
    private val calibrationUseCase: CalibrationUseCase,
) : ViewModel() {

    val appState = appRepository.appState
    var onToggleRecording = appRepository.onToggleRecording

    init {
        viewModelScope.launch {
            appRepository.appState.collect {
                when (it) {
                    AppState.Idle -> {}
                    AppState.Calibrating -> {}

                    AppState.Recording -> {
                        startRecording()
                    }

                    AppState.Ready -> {
                        stopRecording()
                    }
                }
            }
        }
    }

    fun startRecording() {
        Timber.i("START RECORDING")
        appRepository.setActivityId(System.currentTimeMillis())
        recordSensorDataUseCase.startRecording(appRepository.activityId.value)
    }

    fun stopRecording() {
        Timber.i("STOP RECORDING")
        recordSensorDataUseCase.stopRecording()
        appRepository.setActivityId(0)
    }

    fun startCalibration() {
        Timber.i("START CALIBRATION")
        viewModelScope.launch(Dispatchers.IO) {
            calibrationUseCase.invoke().collect {
                Timber.i("PROCESSING CALIBRATION: ${it.isCalibrated}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.i("CLEARED VIEWMODEL")
    }
}