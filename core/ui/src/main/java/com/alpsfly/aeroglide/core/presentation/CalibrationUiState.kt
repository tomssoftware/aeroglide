package com.alpsfly.aeroglide.core.presentation

import com.alpsfly.aeroglide.core.model.hardware.Calibration

sealed interface  CalibrationUiState {
    data object Loading : CalibrationUiState
    data class Success(
        val calibration: Calibration,
    ) : CalibrationUiState
}