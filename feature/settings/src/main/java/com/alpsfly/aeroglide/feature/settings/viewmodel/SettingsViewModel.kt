package com.alpsfly.aeroglide.feature.settings.viewmodel

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Helper extension function for SharedPreferences
fun SharedPreferences.getFloat(key: String, defaultValue: Float): Float {
    return getString(key, defaultValue.toString())?.toFloatOrNull() ?: defaultValue
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val prefs: SharedPreferences // Inject SharedPreferences via Hilt
) : ViewModel() {

    // Auto-Start Settings
    private val _autoStartEnabled = MutableStateFlow(false)
    val autoStartEnabled = _autoStartEnabled.asStateFlow()

    private val _autoStartSpeed = MutableStateFlow(0f)
    val autoStartSpeed = _autoStartSpeed.asStateFlow()

    private val _autoStartClimbRate = MutableStateFlow(0f)
    val autoStartClimbRate = _autoStartClimbRate.asStateFlow()

    // Vario Tone Thresholds
    private val _varioClimbThreshold = MutableStateFlow(0f)
    val varioClimbThreshold = _varioClimbThreshold.asStateFlow()

    private val _varioSinkThreshold = MutableStateFlow(0f)
    val varioSinkThreshold = _varioSinkThreshold.asStateFlow()

    var onAutoStartEnabled = appRepository.onAutoStartEnabled
    var onAutoStartDisabled = appRepository.onAutoStartDisabled

    // Add other states for your EditText and Switch preferences here...

    init {
        loadSettings()
    }

    fun setAutoStartEnabled(enabled: Boolean) {
        _autoStartEnabled.value = enabled
        if (enabled) {
            onAutoStartEnabled()
        } else {
            onAutoStartDisabled()
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // Load initial values from SharedPreferences
            // NOTE: Replace "spk_auto_start_speed" with your actual preference key (R.string.spk_auto_start_speed)
            _autoStartSpeed.value = prefs.getInt("spk_auto_start_speed", 20).toFloat()
            _autoStartClimbRate.value = prefs.getInt("spk_auto_start_climbrate", 5).toFloat() / 10f
            _varioClimbThreshold.value = prefs.getInt("spk_vario_tone_threshold_climb", 2).toFloat() / 10f
            _varioSinkThreshold.value = prefs.getInt("spk_vario_tone_threshold_sink", -30).toFloat() / -10f

            // Load other settings...
        }
    }

    fun onAutoStartSpeedChange(newValue: Float) {
        _autoStartSpeed.value = newValue
        prefs.edit {
            // SeekBarPreference stored an Int, so we convert back
            putInt("spk_auto_start_speed", newValue.toInt())
        }
    }

    fun onAutoStartClimbRateChange(newValue: Float) {
        // The value is stored as an Int (e.g., 5 for 0.5 m/s)
        val storedValue = (newValue * 10).toInt()
        _autoStartClimbRate.value = storedValue / 10f
        prefs.edit {
            putInt("spk_auto_start_climbrate", storedValue)
        }
    }

    fun onVarioClimbThresholdChange(newValue: Float) {
        val storedValue = (newValue * 10).toInt()
        _varioClimbThreshold.value = storedValue / 10f
        prefs.edit {
            putInt("spk_vario_tone_threshold_climb", storedValue)
        }
    }

    fun onVarioSinkThresholdChange(newValue: Float) {
        // Handle negative value storage
        val storedValue = (newValue * -10).toInt()
        _varioSinkThreshold.value = storedValue / -10f
        prefs.edit {
            putInt("spk_vario_tone_threshold_sink", storedValue)
        }
    }

    // Add update functions for other settings...
}
