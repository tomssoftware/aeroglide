package com.alpsfly.aeroglide.core.firebase

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun <T> DocumentReference.addSnapshotListenerFlow(dataType: Class<T>): Flow<Response<T?>> = callbackFlow {
    val listener = EventListener<DocumentSnapshot> { snapshot, error ->
        val response = if (snapshot != null) {
            val data = snapshot.toObject(dataType)
            Response.Success(data)
        } else {
            Response.Error(error?.message ?: error.toString())
        }
        trySend(response).isSuccess
    }

    val registration = addSnapshotListener(listener)
    awaitClose { registration.remove() }
}
