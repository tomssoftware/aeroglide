package com.alpsfly.aeroglide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.model.common.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AeroGlideViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
    private val dataRepository: DataRepository,
) : ViewModel() {
    private val enableRecording = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            enableRecording.flatMapLatest { enable ->
                if (enable) {
                    @OptIn(FlowPreview::class)
                    sensorRepository.altitudeFlow.sample(1000.milliseconds)
                } else {
                    emptyFlow()
                }
            }.collect { altitude ->
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        dataRepository.addUser(
                            User(
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                ""
                            )
                        )
                    }
                }
            }
        }
    }

    fun startRecording() {
        enableRecording.value = true
    }

    fun stopRecording() {
        enableRecording.value = false
    }

    fun isRecording() = enableRecording.value
}