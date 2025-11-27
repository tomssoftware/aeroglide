package com.alpsfly.aeroglide

import androidx.lifecycle.ViewModel
import com.alpsfly.aeroglide.core.domain.usecase.FlightSessionCoordinatorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AeroGlideViewModel @Inject constructor(
    private val flightCoordinator: FlightSessionCoordinatorUseCase
) : ViewModel() {

    fun onToggleRecording() = flightCoordinator.onToggleRecording()
    fun onReCalibrate() = flightCoordinator.onStartCalibration()

    override fun onCleared() {
        super.onCleared()
        Timber.i("CLEARED VIEWMODEL")
    }
}