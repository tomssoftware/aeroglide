package de.tomssoftware.aeroglide.core.data

import de.tomssoftware.aeroglide.core.model.firebase.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?> // Wrap FirebaseUser in your own domain model
    suspend fun createUserWithEmail(fullName: String, email: String, password: String): Result<Unit>
    suspend fun signInWithGoogle(idToken: String): Result<Unit>
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    suspend fun signOut()
    fun isUserLoggedIn(): Boolean
}

data class User(val id: String, val email: String?, val displayName: String?)
