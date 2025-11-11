package com.alpsfly.aeroglide.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.domain.usecase.state.AppState
import com.alpsfly.aeroglide.core.domain.usecase.state.AppStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min


@HiltViewModel
open class LineChartViewModel @Inject constructor(
    appStateManager: AppStateManager,
    appRepository: AppRepository,
    private val dataRepository: DataRepository
) : ViewModel() {
    private val activityId = appRepository.activityId
    protected val appState = appStateManager.appState
    protected var minAltitude = 10.0
    protected var maxAltitude = 0.0
    protected var minClimbrate = -0.25
    protected var maxClimbrate = 0.25

    init {
        viewModelScope.launch {
            collectActivity()
        }
    }

    private suspend fun collectActivity() {
        activityId.collect { activityId ->
            if (appState.value == AppState.Recording) {
                dataRepository.getActivityFlow(activityId).collect { activity ->
                    activity?.let { activity ->
                        if (maxAltitude == 0.0) {
                            maxAltitude = activity.maxAltitude.toDouble()
                        }
                        if (minAltitude == 10.0) {
                            minAltitude = activity.minAltitude.toDouble()
                        }

                        maxAltitude = max(maxAltitude, activity.maxAltitude.toDouble())
                        minAltitude = min(minAltitude, activity.minAltitude.toDouble())
                        maxClimbrate = max(maxClimbrate, activity.maxClimbrate.toDouble())
                        minClimbrate = min(minClimbrate, activity.minClimbrate.toDouble())
                    }
                }
            }
        }
    }
}


