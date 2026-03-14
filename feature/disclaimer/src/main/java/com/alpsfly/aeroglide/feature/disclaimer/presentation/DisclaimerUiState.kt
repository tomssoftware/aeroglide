package com.alpsfly.aeroglide.feature.disclaimer.presentation

import com.alpsfly.aeroglide.core.model.database.Calibration

sealed interface  DisclaimerUiState {
    data object Loading : DisclaimerUiState
    data class Success(
        val measured: Boolean,
    ) : DisclaimerUiState
}