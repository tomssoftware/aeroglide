package com.alpsfly.aeroglide.core.viewmodel

import androidx.lifecycle.ViewModel
import com.alpsfly.aeroglide.core.data.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.sample
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SensorViewModel @Inject constructor(
    sensorRepository: SensorRepository,
) : ViewModel() {
}
