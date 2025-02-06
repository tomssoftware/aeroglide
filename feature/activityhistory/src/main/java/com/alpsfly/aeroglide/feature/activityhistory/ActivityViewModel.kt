package com.alpsfly.aeroglide.feature.activityhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.model.database.Activity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityHistoryViewModel @Inject constructor(
    private val dataRepository: DataRepository,
) : ViewModel() {

    val allActivitiesUiState: StateFlow<ActivityListUiState> =
        dataRepository.allActivitiesFlow
            .map { list -> ActivityListUiState.Success(list) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ActivityListUiState.Loading
            )

    private val _activity = MutableStateFlow<ActivityUiState>(ActivityUiState.Loading)
    val activity: StateFlow<ActivityUiState> get() = _activity

    fun loadActivityById(id: Long) {
        viewModelScope.launch {
            _activity.value = ActivityUiState.Loading
            _activity.value = ActivityUiState.Success(
                dataRepository.getActivity(id)
            )
        }
    }
}

sealed interface ActivityUiState {
    data object Loading : ActivityUiState
    data class Success(
        val item: Activity
    ) : ActivityUiState
}

sealed interface ActivityListUiState {
    data object Loading : ActivityListUiState
    data class Success(
        val list: List<Activity>,
    ) : ActivityListUiState
}