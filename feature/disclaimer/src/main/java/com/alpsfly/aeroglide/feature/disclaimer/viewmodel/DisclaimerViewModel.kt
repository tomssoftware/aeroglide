package com.alpsfly.aeroglide.feature.disclaimer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.domain.usecase.FrequencyMeasureUseCase
import com.alpsfly.aeroglide.feature.disclaimer.presentation.DisclaimerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DisclaimerViewModel @Inject constructor(
    frequencyMeasureUseCase: FrequencyMeasureUseCase
) : ViewModel() {

//    val disclaimerUiState: StateFlow<DisclaimerUiState> =
//        frequencyMeasureUseCase().map { result ->
//            if (!result) {
//                DisclaimerUiState.Loading
//            } else {
//                DisclaimerUiState.Success(result)
//            }
//        }.stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5000),
//            initialValue = DisclaimerUiState.Loading
//        )
}