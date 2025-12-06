package com.alpsfly.aeroglide.core.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    init {
        //if (BuildConfig.DEBUG) {
        auth.useEmulator("10.0.2.2", 9099)
        //}
    }

    override val currentUser = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser?.let {
                User(it.uid, it.email, it.displayName)
            }
            trySend(user)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun isUserLoggedIn(): Boolean = auth.currentUser != null

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await() // Requires kotlinx-coroutines-play-services
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithEmail(email: String, link: String): Result<Unit> {
        return try {
            if (auth.isSignInWithEmailLink(link)) {
                auth.signInWithEmailLink(email, link).await()
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Invalid email link"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}