package com.alpsfly.aeroglide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.domain.usecase.StartRecordActivityUseCase
import com.alpsfly.aeroglide.core.model.database.Activity
import com.alpsfly.aeroglide.core.model.hardware.SensorType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import javax.inject.Inject

@HiltViewModel
class AeroGlideViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val dataRepository: DataRepository,
    private val recordSensorDataUseCase: StartRecordActivityUseCase,
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
    }
}