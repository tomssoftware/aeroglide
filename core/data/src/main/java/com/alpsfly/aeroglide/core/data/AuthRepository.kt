package com.alpsfly.aeroglide.core.data

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?> // Wrap FirebaseUser in your own domain model
    suspend fun signInWithGoogle(idToken: String): Result<Unit>
    suspend fun signInWithEmail(email: String, link: String): Result<Unit>
    suspend fun signOut()
    fun isUserLoggedIn(): Boolean
}

data class User(val id: String, val email: String?, val displayName: String?)
