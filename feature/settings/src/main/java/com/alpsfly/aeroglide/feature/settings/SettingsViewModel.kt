package com.alpsfly.aeroglide.feature.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpsfly.aeroglide.core.data.AuthRepository
import com.alpsfly.aeroglide.core.domain.usecase.state.AppStateManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
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
    private val authRepository: AuthRepository,
    private val appStateManager: AppStateManager,
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

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // Load initial values from SharedPreferences
            _autoStartEnabled.value = prefs.getBoolean("spk_auto_start_enabled", false)
            _autoStartSpeed.value = prefs.getInt("spk_auto_start_speed", 20).toFloat()
            _autoStartClimbRate.value = prefs.getInt("spk_auto_start_climbrate", 5).toFloat() / 10f
            _varioClimbThreshold.value =
                prefs.getInt("spk_vario_tone_threshold_climb", 2).toFloat() / 10f
            _varioSinkThreshold.value =
                prefs.getInt("spk_vario_tone_threshold_sink", -30).toFloat() / -10f

            // Load other settings...
        }
    }

    fun onAutoStartEnabled(enabled: Boolean) {
        _autoStartEnabled.value = enabled
        prefs.edit {
            putBoolean("spk_auto_start_enabled", enabled)
        }
        appStateManager.onAutoStartEnabled(enabled)
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

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("YOUR_WEB_CLIENT_ID_FROM_FIREBASE_CONSOLE")
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                if (credential is androidx.credentials.CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    // Call Repository to sign in with Firebase
                    authRepository.signInWithGoogle(googleIdTokenCredential.idToken)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
