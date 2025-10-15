package com.alpsfly.aeroglide.core.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface AppRepository {
    val activityId: StateFlow<Long>
    val isRecording: StateFlow<Boolean>
    val isCalibrationRunning: StateFlow<Boolean>
    fun startRecording(activityId: Long)
    fun stopRecording()
    fun startCalibration()
    fun stopCalibration()
}

@Singleton
class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppRepository {
    private val _activityId = MutableStateFlow(0L)
    override val activityId = _activityId.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    override val isRecording = _isRecording.asStateFlow()

    private val _isCalibrationRunning = MutableStateFlow(false)
    override val isCalibrationRunning = _isCalibrationRunning.asStateFlow()

    override fun startRecording(activityId: Long) {
        check(activityId != 0L)
        _isRecording.value = true
        _activityId.value = activityId
    }

    override fun stopRecording() {
        check(activityId.value != 0L)
        _isRecording.value = false
        _activityId.value = 0L
    }

    override fun startCalibration() {
        _isCalibrationRunning.value = true
    }

    override fun stopCalibration() {
        _isCalibrationRunning.value = false
    }
}