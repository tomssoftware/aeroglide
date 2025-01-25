package com.alpsfly.aeroglide.feature.activityhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.model.database.Activity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ActivityHistoryViewModel @Inject constructor(
    private val dataRepository: DataRepository,
) : ViewModel() {

    val allActivitiesUiState: StateFlow<ActivityListUiState> =
        dataRepository.allActivities.map { list ->
            ActivityListUiState.Success(list)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ActivityListUiState.Loading
        )

    fun getActivityUiState(id: Long): StateFlow<ActivityUiState> {
        return dataRepository.getActivity(id).map { activity ->
            ActivityUiState.Success(activity)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ActivityUiState.Loading
        )
    }
}

sealed interface ActivityUiState {
    data object Loading : ActivityUiState
    data class Success(
        val activity: Activity
    ) : ActivityUiState
}

sealed interface ActivityListUiState {
    data object Loading : ActivityListUiState
    data class Success(
        val activityHistory: List<Activity>,
    ) : ActivityListUiState
}