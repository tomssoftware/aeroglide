// feature/auth/src/main/java/.../LoginViewModel.kt
package de.tomssoftware.aeroglide.feature.authentication

import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tomssoftware.aeroglide.core.data.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPasswordVisible: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }
    fun onTogglePasswordVisibility() = _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun signInWithEmail() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Please fill in all fields") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.signInWithEmail(state.email, state.password)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, error = null) }
            } else {
                val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Login failed"
                _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val credentialManager = CredentialManager.create(context)

                // 1. Build the Google Option
                // REPLACE WITH YOUR WEB CLIENT ID from Firebase Console
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("YOUR_WEB_CLIENT_ID.apps.googleusercontent.com")
                    .setAutoSelectEnabled(true)
                    .build()

                // 2. Create Request
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // 3. Launch System Dialog
                val result = credentialManager.getCredential(context, request)
                handleCredentialResult(result.credential)

            } catch (e: GetCredentialException) {
                Timber.w(e, "Google Sign In cancelled or failed")
                _uiState.update { it.copy(isLoading = false) } // Just stop loading, don't show error if cancelled
            } catch (e: Exception) {
                Timber.e(e, "Unexpected error")
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    private suspend fun handleCredentialResult(credential: Credential) {
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

            // 4. Pass Token to Repository to auth with Firebase
            val result = authRepository.signInWithGoogle(googleIdTokenCredential.idToken)

            if (result.isSuccess) {
                // Navigation logic will observe User state, so we just stop loading here
                _uiState.update { it.copy(isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Firebase Auth Failed") }
            }
        } else {
            _uiState.update { it.copy(isLoading = false, error = "Unknown Credential Type") }
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.signInAnonymously()
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.localizedMessage) }
            }
        }
    }
}
