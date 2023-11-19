package com.alpsfly.aeroglide.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.alpsfly.aeroglide.data.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewScoped
import javax.inject.Inject

@HiltViewModel
class SensorViewModel @Inject constructor(
    private val sensorRepository: SensorRepository
) : ViewModel() {

    init {
        Log.d("SensorViewModel", "init")
    }

    fun getData() = sensorRepository.getData()
}