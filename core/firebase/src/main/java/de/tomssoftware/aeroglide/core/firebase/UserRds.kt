package de.tomssoftware.aeroglide.core.firebase

import de.tomssoftware.aeroglide.core.model.firebase.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User Remote Data Source (RDS).
 * Handles all Firestore operations related to the 'users' collection.
 * Symmetric to UserDao.
 */
@Singleton
class UserRds @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    // Using a constant or simple string for the collection name
    // This matches CloudStorage.USERS usually
    private val collection get() = firestore.collection("users")

    /**
     * Creates or overwrites a user in Firestore.
     * We use 'set' because the User ID is provided by Auth (we don't generate a random ID).
     */
    suspend fun addUser(user: User): Result<Unit> {
        return try {
            collection
                .document(user.uid)
                .set(user)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates an existing user.
     * Using SetOptions.merge() ensures we don't accidentally wipe fields
     * if the User object passed is partial (though typically you send the full object).
     */
    suspend fun updateUser(userId: String, user: User): Result<Unit> {
        return try {
            collection.document(userId).set(user, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a user from Firestore.
     */
    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            collection.document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches a user snapshot.
     * Useful for one-time syncs.
     */
    suspend fun getUser(userId: String): Result<User?> {
        return try {
            val snapshot = collection.document(userId).get().await()
            if (snapshot.exists()) {
                Result.success(snapshot.toObject(User::class.java))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
