package com.alpsfly.aeroglide.core.firebase

import com.alpsfly.aeroglide.core.model.common.Pilot
import com.alpsfly.aeroglide.core.model.firebase.Activity as ActivityDto
import com.alpsfly.aeroglide.core.model.firebase.Blacklist
import com.alpsfly.aeroglide.core.model.firebase.Purchase
import com.alpsfly.aeroglide.core.model.firebase.User
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudStorageFirebase @Inject constructor() : CloudStorage {
    private val firestore = Firebase.firestore
    private val crashlytics = Firebase.crashlytics

    override suspend fun readBlacklists(): Flow<Response<Blacklist?>> = flow {
        try {
            emit(Response.Processing)
            val task = firestore
                .collection(CloudStorage.CONFIGS)
                .document(CloudStorage.BLACKLIST)
                .get()
                .await()
            val blacklist = task.toObject(Blacklist::class.java)
            emit(Response.Success(blacklist))
        } catch (e: Exception) {
            emit((Response.Error(e.message ?: e.toString())))
        }
    }

    override suspend fun readPurchase(purchaseToken: String): Flow<Response<Purchase?>> = callbackFlow {
        val ref = firestore
            .collection(CloudStorage.PURCHASES)
            .document(purchaseToken)
        val ssl = ref.addSnapshotListener { value, error ->
            val response = if (value != null) {
                val data = value.toObject(Purchase::class.java)
                Response.Success(data)
            } else {
                Response.Error(error?.message ?: error.toString())
            }
            trySend(response).isSuccess
        }
        awaitClose {
            ssl.remove()
        }
    }

    override suspend fun writeUser(userId: String, data: User) = flow {
        try {
            emit(Response.Processing)
            firestore
                .collection(CloudStorage.USERS)
                .document(userId)
                .set(data)
                .await()
            emit(Response.Success(true))
        } catch (e: Exception) {
            emit((Response.Error(e.message ?: e.toString())))
        }
    }

    override suspend fun addUserCallbackCollector(userId: String): Flow<Response<User?>> = callbackFlow {
        val ref = firestore
            .collection(CloudStorage.USERS)
            .document(userId)
        val ssl = ref.addSnapshotListener { value, error ->
            val response = if (value != null) {
                val data = value.toObject(User::class.java)
                Response.Success(data)
            } else {
                Response.Error(error?.message ?: error.toString())
            }
            trySend(response).isSuccess
        }
        awaitClose {
            ssl.remove()
        }
    }

    override suspend fun writePilot(userId: String, data: Pilot) = flow {
        try {
            emit(Response.Processing)
            firestore
                .collection(CloudStorage.PILOTS)
                .document(userId)
                .set(data)
                .await()
            emit(Response.Success(true))
        } catch (e: Exception) {
            emit((Response.Error(e.message ?: e.toString())))
        }
    }

    override suspend fun readPilot(userId: String): Flow<Response<Pilot?>> = flow {
        try {
            emit(Response.Processing)
            val task = firestore
                .collection(CloudStorage.PILOTS)
                .document(userId)
                .get()
                .await()
            val pilot = task.toObject(Pilot::class.java)
            emit(Response.Success(pilot))
        } catch (e: Exception) {
            emit((Response.Error(e.message ?: e.toString())))
        }
    }

    override suspend fun addPilotCallbackCollector(userId: String) = callbackFlow {
        val ref = firestore
            .collection(CloudStorage.PILOTS)
            .document(userId)
        val ssl = ref.addSnapshotListener { value, error ->
            val response = if (value != null) {
                val data = value.toObject(Pilot::class.java)
                Response.Success(data)
            } else {
                Response.Error(error?.message ?: error.toString())
            }
            trySend(response).isSuccess
        }
        awaitClose {
            ssl.remove()
        }
    }

    override suspend fun incStatisticCounter(userId: String, documentName: String, fieldName: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis()))
        val collection = firestore
            .collection(CloudStorage.STATISTICS)
            .document(userId)
            .collection(timestamp)
        val document = collection
            .document(documentName)
        val field = hashMapOf(
            fieldName to 0
        )

        document.get()
            .addOnCompleteListener { taskGet ->
                if (taskGet.isSuccessful) {
                    if (taskGet.result.exists()) {
                        document.update(fieldName, FieldValue.increment(1))
                            .addOnCompleteListener { taskUpdate ->
                                if (!taskUpdate.isSuccessful) {
                                    crashlytics.recordException(Exception("Update '${documentName}' document failed!"))
                                }
                            }
                    } else {
                        collection.document(documentName).set(field)
                            .addOnCompleteListener { taskSet ->
                                if (!taskSet.isSuccessful) {
                                    crashlytics.recordException(Exception("Insert '${documentName} document' failed!"))
                                }
                            }
                            .addOnFailureListener {
                                Timber.w(it.message)
                            }
                    }
                }
            }
            .addOnFailureListener {
                Timber.w(it.message)
            }
    }

    override suspend fun updatePurchase(purchase: Purchase) {
        val data = hashMapOf(
            "userId" to purchase.userId,
            "fcmToken" to purchase.fcmToken,
            "purchaseToken" to purchase.purchaseToken
        )

        firestore
            .collection("purchases")
            .document(purchase.purchaseToken)
            .set(data, SetOptions.merge())
            .addOnFailureListener { e ->
                crashlytics.recordException(Exception("Error update purchase document! ${e.message}"))
            }
    }

    // -------------------------------------------------------------------------
    // Activity sync  –  /users/{userId}/activities/{activityId}
    // -------------------------------------------------------------------------

    override suspend fun writeActivity(userId: String, activity: ActivityDto): String {
        val collection = firestore
            .collection(CloudStorage.USERS)
            .document(userId)
            .collection(CloudStorage.ACTIVITIES)

        return if (activity.id == null) {
            // New activity – Firestore generates the document ID
            val docRef = collection.add(activity).await()
            docRef.id
        } else {
            // Existing activity – full overwrite
            val existingId = activity.id!!
            collection.document(existingId).set(activity).await()
            existingId
        }
    }

    override suspend fun readActivitiesForUser(userId: String): List<ActivityDto> {
        val snapshot = firestore
            .collection(CloudStorage.USERS)
            .document(userId)
            .collection(CloudStorage.ACTIVITIES)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(ActivityDto::class.java) }
    }

    override suspend fun deleteActivity(userId: String, firestoreId: String) {
        firestore
            .collection(CloudStorage.USERS)
            .document(userId)
            .collection(CloudStorage.ACTIVITIES)
            .document(firestoreId)
            .delete()
            .await()
    }
}
