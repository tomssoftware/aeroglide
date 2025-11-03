package com.alpsfly.aeroglide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.AppState
import com.alpsfly.aeroglide.core.domain.usecase.AutoStartUseCase
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
    private val autoStartUseCase: AutoStartUseCase
) : ViewModel() {

    val appState = appRepository.appState
    var onToggleRecording = appRepository.onToggleRecording
    fun doStartCalibration() = appRepository.doStartCalibration()
    fun doStopCalibration() = appRepository.doStopCalibration()
    fun doEnableAutoStart(enable: Boolean) = appRepository.doEnableAutoStart(enable)

    init {
        viewModelScope.launch {
            appRepository.appState.collect {
                when (it) {
                    AppState.Idle -> {
                        Timber.i("AppState.Idle")
                    }

                    AppState.Calibrating -> {
                        Timber.i("AppState.Calibrating")
                        startCalibration()
                    }

                    AppState.Ready -> {
                        Timber.i("AppState.Ready")
                        stopRecording()
                    }

                    AppState.AutoStart -> {
                        Timber.i("AppState.AutoStart")
                        enableAutoStart()
                    }

                    AppState.Recording -> {
                        Timber.i("AppState.Recording")
                        startRecording()
                    }
                }
            }
        }
    }

    private fun startRecording() {
        Timber.d("startRecording")
        appRepository.setActivityId(System.currentTimeMillis())
        recordSensorDataUseCase.startRecording(appRepository.activityId.value)
    }

    private fun stopRecording() {
        Timber.d("stopRecording")
        recordSensorDataUseCase.stopRecording()
        appRepository.setActivityId(0)
    }

    private fun startCalibration() {
        Timber.d("startCalibration")
        viewModelScope.launch(Dispatchers.IO) {
            calibrationUseCase.invoke().collect {
                Timber.d("processCalibration: ${it.isCalibrated}")
            }
        }
    }

    private fun enableAutoStart() {
        autoStartUseCase.enableAutoStart()
    }

    private fun disableAutoStart() {
        autoStartUseCase.disableAutoStart()
    }

    override fun onCleared() {
        super.onCleared()
        Timber.i("CLEARED VIEWMODEL")
    }
}