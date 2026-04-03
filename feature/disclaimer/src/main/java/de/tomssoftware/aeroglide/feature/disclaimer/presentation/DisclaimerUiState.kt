package de.tomssoftware.aeroglide.feature.disclaimer.presentation

import de.tomssoftware.aeroglide.core.model.database.Calibration

sealed interface  DisclaimerUiState {
    data object Loading : DisclaimerUiState
    data class Success(
        val measured: Boolean,
    ) : DisclaimerUiState
}