package de.tomssoftware.aeroglide.sync.status

import de.tomssoftware.aeroglide.sync.initializers.SYNC_TOPIC
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Implementation of [SyncSubscriber] that subscribes to the FCM [SYNC_TOPIC].
 */
internal class FirebaseSyncSubscriber @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging,
) : SyncSubscriber {
    override suspend fun subscribe() {
        firebaseMessaging
            .subscribeToTopic(SYNC_TOPIC)
            .await()
    }
}

