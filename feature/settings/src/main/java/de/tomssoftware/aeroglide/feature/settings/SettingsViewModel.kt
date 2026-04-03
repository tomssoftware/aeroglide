package de.tomssoftware.aeroglide.feature.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tomssoftware.aeroglide.core.data.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Manages user-configurable settings for auto-start detection and variometer tone thresholds,
 * and handles Google Sign-In via [AuthRepository].
 *
 * Settings are persisted to [SharedPreferences] on every change and reloaded on construction
 * so the UI always reflects the last saved values without a repository layer.
 *
 * Auto-start activation is purely setting-driven: persisting the flag here is sufficient –
 * [de.tomssoftware.aeroglide.core.domain.usecase.FlightSessionCoordinatorUseCase] observes
 * [de.tomssoftware.aeroglide.core.data.AutoStartSettingsProvider.isAutoStartEnabled] directly
 * and enables/disables the auto-start processor whenever the value changes.
 *
 * @see AuthRepository
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    // SharedPreferences is provided as a Hilt binding so the key namespace
    // is controlled at the injection site rather than hardcoded here.
    private val prefs: SharedPreferences,
) : ViewModel() {

    // --- Auto-Start Settings ---

    /** Whether automatic take-off and landing detection is active. */
    private val _autoStartEnabled = MutableStateFlow(false)
    val autoStartEnabled = _autoStartEnabled.asStateFlow()

    /** Minimum ground speed in km/h required to consider the pilot airborne. */
    private val _autoStartSpeed = MutableStateFlow(0f)
    val autoStartSpeed = _autoStartSpeed.asStateFlow()

    /** Minimum climb rate in m/s required to confirm a take-off. */
    private val _autoStartClimbRate = MutableStateFlow(0f)
    val autoStartClimbRate = _autoStartClimbRate.asStateFlow()

    // --- Variometer Settings ---

    /** Climb rate in m/s above which the variometer emits a climb tone. */
    private val _varioClimbThreshold = MutableStateFlow(0f)
    val varioClimbThreshold = _varioClimbThreshold.asStateFlow()

    /** Sink rate in m/s (negative) below which the variometer emits a sink tone. */
    private val _varioSinkThreshold = MutableStateFlow(0f)
    val varioSinkThreshold = _varioSinkThreshold.asStateFlow()

    init {
        loadSettings()
    }

    // --- Internal Helpers ---

    /** Populates all [MutableStateFlow]s from persisted [SharedPreferences] values. */
    private fun loadSettings() {
        viewModelScope.launch {
            _autoStartEnabled.value = prefs.getBoolean("spk_auto_start_enabled", false)
            // Speed stored as km/h (Float); AutoStartSettingsProviderImpl converts to m/s on read.
            _autoStartSpeed.value = prefs.getFloat("spk_auto_start_speed", 20f)
            _autoStartClimbRate.value = prefs.getFloat("spk_auto_start_climbrate", 0.5f)
            _varioClimbThreshold.value = prefs.getFloat("spk_vario_tone_threshold_climb", 0.2f)
            _varioSinkThreshold.value = prefs.getFloat("spk_vario_tone_threshold_sink", -0.3f)
        }
    }

    // --- Public API ---

    /**
     * Enables or disables auto-start detection and persists the choice to [SharedPreferences].
     *
     * No direct state-machine interaction is needed here: [de.tomssoftware.aeroglide.core.domain
     * .usecase.FlightSessionCoordinatorUseCase] observes
     * [de.tomssoftware.aeroglide.core.data.AutoStartSettingsProvider.isAutoStartEnabled] and
     * automatically enables or disables the auto-start processor whenever this value changes,
     * as long as the app is in [de.tomssoftware.aeroglide.core.domain.usecase.state.AppState.Ready].
     */
    fun onAutoStartEnabled(enabled: Boolean) {
        _autoStartEnabled.value = enabled
        prefs.edit { putBoolean("spk_auto_start_enabled", enabled) }
    }

    /**
     * Updates the auto-start ground-speed threshold and persists it.
     *
     * @param newValue Speed in km/h as shown in the UI.
     *                 `AutoStartSettingsProviderImpl` converts to m/s on read via `/ 3.6f`.
     */
    fun onAutoStartSpeedChange(newValue: Float) {
        _autoStartSpeed.value = newValue
        prefs.edit { putFloat("spk_auto_start_speed", newValue) }
    }

    /**
     * Updates the auto-start climb-rate threshold and persists it.
     *
     * @param newValue Climb rate in m/s as shown in the UI (e.g. `0.5`).
     *                 Stored and read as Float directly – no scaling required.
     */
    fun onAutoStartClimbRateChange(newValue: Float) {
        _autoStartClimbRate.value = newValue
        prefs.edit { putFloat("spk_auto_start_climbrate", newValue) }
    }

    /** Updates the variometer climb-tone threshold and persists it. */
    fun onVarioClimbThresholdChange(newValue: Float) {
        _varioClimbThreshold.value = newValue
        prefs.edit { putFloat("spk_vario_tone_threshold_climb", newValue) }
    }

    /**
     * Updates the variometer sink-tone threshold and persists it.
     *
     * @param newValue Negative float in m/s (e.g. `-0.5` for 0.5 m/s sink).
     */
    fun onVarioSinkThresholdChange(newValue: Float) {
        _varioSinkThreshold.value = newValue
        prefs.edit { putFloat("spk_vario_tone_threshold_sink", newValue) }
    }

    // --- Authentication ---

    /**
     * Launches a Google Sign-In flow using [CredentialManager] and forwards the resulting
     * ID token to [AuthRepository.signInWithGoogle].
     *
     * @param context Activity context required by [CredentialManager] to present the
     *                account-picker bottom sheet.
     */
    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                // TODO: Replace hardcoded stub with the actual Web Client ID from
                //       google-services.json / Firebase Console (issue #?).
                .setServerClientId("YOUR_WEB_CLIENT_ID_FROM_FIREBASE_CONSOLE")
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                if (credential is androidx.credentials.CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    authRepository.signInWithGoogle(googleIdTokenCredential.idToken)
                }
            } catch (_: Exception) {
                // TODO: Surface sign-in errors to the UI via a dedicated error state
                //       instead of silently swallowing the exception (issue #?).
            }
        }
    }
}
