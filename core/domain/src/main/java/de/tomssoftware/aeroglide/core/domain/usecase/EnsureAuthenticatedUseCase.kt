package de.tomssoftware.aeroglide.core.domain.usecase

import de.tomssoftware.aeroglide.core.data.AuthRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Stellt sicher, dass beim App-Start immer ein Firebase-User existiert.
 * Ist kein User angemeldet, wird automatisch eine anonyme Session erstellt.
 * Die Auth-UI (E-Mail / Google) ist ausschließlich für das bewusste Upgrade
 * von der anonymen zur echten Identität vorgesehen.
 */
class EnsureAuthenticatedUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        if (!authRepository.isUserLoggedIn()) {
            Timber.i("User not logged in, signing in anonymously")
            authRepository.signInAnonymously()
        }
    }
}

