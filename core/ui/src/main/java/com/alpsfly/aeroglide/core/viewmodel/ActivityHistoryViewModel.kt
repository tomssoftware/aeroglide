package com.alpsfly.aeroglide.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.model.database.Activity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ActivityHistoryViewModel @Inject constructor(
    sensorRepository: SensorRepository,
    dataRepository: DataRepository,
) : ViewModel() {

    val activityHistoryUiState: StateFlow<ActivityHistoryUiState> =
        dataRepository.activity.map { list ->
            val activityHistoryDataList = list.map { activity ->
                ActivityHistoryData(
                    date = activity.begin,
                    time = activity.begin,
                    distance = activity.distance
                )
            }
            ActivityHistoryUiState.Success(activityHistoryDataList)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ActivityHistoryUiState.Loading
        )
}

data class ActivityHistoryData(
    val date: Long,
    val time: Long,
    val distance: Float
)

sealed interface ActivityHistoryUiState {
    data object Loading : ActivityHistoryUiState
    data class Success(
        val activityHistory: List<ActivityHistoryData>,
    ) : ActivityHistoryUiState
}