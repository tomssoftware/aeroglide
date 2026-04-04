package de.tomssoftware.aeroglide.core.data

import de.tomssoftware.aeroglide.core.firebase.UserRds
import de.tomssoftware.aeroglide.core.model.firebase.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider.getCredential
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRds: UserRds
) : AuthRepository {

    init {
        //if (BuildConfig.DEBUG) {
        //}
    }

    override val currentUser = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser?.let {
                createNewUser(it)
            }
            trySend(user)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun createUserWithEmail(fullName: String, email: String, password: String): Result<Unit> {
        return try {
            // 1. Create the user in Firebase Auth
            // This validates email/password and checks duplicates
            val authResult =
                auth.createUserWithEmailAndPassword(
                    email,
                    password
                ).await()

            // 2. Get the new unique User ID
            val firebaseUser = authResult.user ?: throw Exception("User creation failed")

            // 3. Create the User object with the correct UID from Firebase
            val newUserWithUid = createNewUser(firebaseUser)

            // 4. Save the extra details (Name, Terms Accepted, etc.) to Firestore
            userRds.addUser(newUserWithUid)

            // 5. Success! The AuthStateListener will automatically fire with the new user.
            Result.success(Unit)
        } catch (e: Exception) {
            // e.g., FirebaseAuthUserCollisionException (email already exists)
            // e.g., FirebaseAuthWeakPasswordException
            Result.failure(e)
        }
    }

    override fun isUserLoggedIn(): Boolean = auth.currentUser != null

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = getCredential(idToken, null)
            auth.signInWithCredential(credential).await() // Requires kotlinx-coroutines-play-services
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(
                email,
                password
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInAnonymously(): Result<Unit> = try {
        auth.signInAnonymously().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun createNewUser(firebaseUser: FirebaseUser): User {
        return User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName,
            photoUrl = firebaseUser.photoUrl?.toString(),
            isEmailVerified = firebaseUser.isEmailVerified,
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis(),
            termsAccepted = true, // Assuming they clicked a checkbox in your UI
            lastVersionCode = 0 //BuildConfig.VERSION_CODE // Access your app version
        )
    }
}
