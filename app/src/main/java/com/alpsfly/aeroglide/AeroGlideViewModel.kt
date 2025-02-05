package com.alpsfly.aeroglide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.domain.usecase.CalibrationUseCase
import com.alpsfly.aeroglide.core.domain.usecase.StartRecordActivityUseCase
import com.alpsfly.aeroglide.core.model.database.Activity
import com.alpsfly.aeroglide.core.model.hardware.SensorType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
@HiltViewModel
class AeroGlideViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val dataRepository: DataRepository,
    private val sensorRepository: SensorRepository,
    private val recordSensorDataUseCase: StartRecordActivityUseCase,
    private val calibrationUseCase: CalibrationUseCase,
) : ViewModel() {

    val isRecording = appRepository.isRecording
    private var activityId = 0L
    fun startRecording() {
        activityId = System.currentTimeMillis()
        dataRepository.setActivityId(activityId)
        appRepository.startRecording(activityId)
    }
    fun stopRecording() {
        activityId = 0L
        appRepository.stopRecording()
    }

    val calibration = sensorRepository.calibration

    init {
        viewModelScope.launch(Dispatchers.IO) {
            isRecording.collect {
                if (it) {
                    recordSensorDataUseCase.startRecording(activityId)
                } else {
                    recordSensorDataUseCase.stopRecording()
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            calibrationUseCase.invoke().collect { calibration ->
                sensorRepository.setCalibration(calibration)
            }
        }
    }
}