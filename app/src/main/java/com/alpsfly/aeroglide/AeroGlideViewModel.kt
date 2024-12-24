package com.alpsfly.aeroglide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.model.local.Track
import com.alpsfly.aeroglide.core.data.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AeroGlideViewModel @Inject constructor(
    sensorRepository: SensorRepository,
    aeroGlideRepository: AeroGlideRepository
) : ViewModel() {
    @OptIn(FlowPreview::class)
    val climbrate = sensorRepository.climbRateFlow.sample(1000.milliseconds)
    @OptIn(FlowPreview::class)
    val altitude = sensorRepository.altitudeFlow.sample(1000.milliseconds)

    private val enableRecording = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            enableRecording.flatMapLatest { enable ->
                if (enable) {
                    altitude
                } else {
                    emptyFlow()
                }
            }.collect { altitude ->
                aeroGlideRepository.insertTrack(Track().apply {
                    this.trackId = altitude.timestamp
                    this.minAltitude = altitude.values[0]
                })
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