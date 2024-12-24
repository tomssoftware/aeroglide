package com.alpsfly.aeroglide.core.data.network.firebase

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.alpsfly.aeroglide.core.data.model.network.Pilot
import com.alpsfly.aeroglide.core.data.model.network.firebase.User
import com.alpsfly.aeroglide.core.data.model.network.firebase.Blacklist
import com.alpsfly.aeroglide.core.data.model.network.firebase.Purchase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class CloudStorageFirebase : CloudStorage {
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

    override suspend fun readPurchase(purchaseToken: String): Flow<Response<Purchase?>> = callbackFlow  {
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
}
