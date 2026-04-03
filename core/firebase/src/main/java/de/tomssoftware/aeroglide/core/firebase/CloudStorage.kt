package de.tomssoftware.aeroglide.core.firebase

import de.tomssoftware.aeroglide.core.model.common.Pilot
import de.tomssoftware.aeroglide.core.model.firebase.Activity as ActivityDto
import de.tomssoftware.aeroglide.core.model.firebase.Blacklist
import de.tomssoftware.aeroglide.core.model.firebase.Purchase
import de.tomssoftware.aeroglide.core.model.firebase.User
import kotlinx.coroutines.flow.Flow

interface CloudStorage {
    suspend fun readBlacklists(): Flow<Response<Blacklist?>>
    suspend fun readPurchase(purchaseToken: String): Flow<Response<Purchase?>>

    suspend fun writeUser(userId: String, data: User): Flow<Response<Boolean>>
    suspend fun addUserCallbackCollector(userId: String): Flow<Response<User?>>

    suspend fun writePilot(userId: String, data: Pilot): Flow<Response<Boolean>>
    suspend fun readPilot(userId: String): Flow<Response<Pilot?>>
    suspend fun addPilotCallbackCollector(userId: String): Flow<Response<Pilot?>>

    suspend fun incStatisticCounter(userId: String, documentName: String, fieldName: String)
    suspend fun updatePurchase(purchase: Purchase)

    // -------------------------------------------------------------------------
    // Activity sync  –  sub-collection: /users/{userId}/activities/{activityId}
    // -------------------------------------------------------------------------

    /**
     * Creates or updates an activity document.
     * - [activity.id] == null  →  Firestore auto-generates the document ID (new activity)
     * - [activity.id] != null  →  full overwrite of the existing document (update)
     *
     * @return the Firestore document ID of the written activity
     */
    suspend fun writeActivity(userId: String, activity: ActivityDto): String

    /**
     * One-shot read of all activity documents for the given user.
     */
    suspend fun readActivitiesForUser(userId: String): List<ActivityDto>

    /**
     * Permanently deletes an activity document from Firestore.
     */
    suspend fun deleteActivity(userId: String, firestoreId: String)

    companion object {

        // root collection paths
        const val USERS = "users"
        const val PILOTS = "pilots"
        const val PURCHASES = "purchases"
        const val STATISTICS = "statistics"
        const val CONFIGS = "configs"

        // sub-collections
        const val ACTIVITIES = "activities"

        // documents
        const val REQUESTS = "requests"
        const val BLACKLIST = "blacklist"

        // fields
        const val DIRECTION_CALL_COUNTER = "directionCallCounter"
        const val VERSIONS = "versions"
    }
}
