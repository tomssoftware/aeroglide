/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tomssoftware.aeroglide.sync.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import de.tomssoftware.aeroglide.core.common.di.IoDispatcher
import de.tomssoftware.aeroglide.core.data.DataRepository
import de.tomssoftware.aeroglide.core.firebase.CloudStorage
import de.tomssoftware.aeroglide.core.model.database.toFirestore
import de.tomssoftware.aeroglide.sync.initializers.SyncConstraints
import de.tomssoftware.aeroglide.sync.initializers.syncForegroundInfo
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException

/**
 * Uploads all locally recorded activities that have not yet been synchronised
 * with Firebase (SyncState.LOCAL or SyncState.PENDING_UPLOAD).
 *
 * Upload strategy ("upload-first"):
 * 1. Fetch all pending activities from the local database.
 * 2. Mark each as PENDING_UPLOAD before attempting the upload
 *    (idempotency guard against parallel worker invocations).
 * 3. On success  → mark SYNCED + store the Firestore document ID.
 * 4. On retryable network error → leave as PENDING_UPLOAD; return [Result.retry].
 * 5. On permanent error → mark ERROR + store the message.
 *
 * NOTE: If the worker is killed after Firestore's `add()` returns but before
 * the local SYNCED update is written, the activity would be uploaded again on
 * the next retry (creating a duplicate). To eliminate this race condition,
 * client-side Firestore document-ID pre-generation would be required.
 * This is tracked as a follow-up improvement.
 */
@HiltWorker
class ActivityUploadWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dataRepository: DataRepository,
    private val cloudStorage: CloudStorage,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun getForegroundInfo(): ForegroundInfo =
        appContext.syncForegroundInfo()

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        Timber.d("ActivityUploadWorker: starting upload run")

        val pending = dataRepository.getPendingActivities()
        Timber.d("ActivityUploadWorker: ${pending.size} activities pending upload")

        if (pending.isEmpty()) return@withContext Result.success()

        var hasRetryableFailure = false

        for (activity in pending) {
            if (activity.userId.isBlank()) {
                Timber.w("ActivityUploadWorker: activity id=${activity.activityId} skipped – userId missing")
                dataRepository.markActivitySyncError(activity.activityId, "Missing userId")
                continue
            }

            // Mark as PENDING_UPLOAD before the network call so that a parallel
            // worker run (or crash-recovery retry) does not double-process it.
            dataRepository.markActivityPendingUpload(activity.activityId)

            try {
                val firestoreId = cloudStorage.writeActivity(
                    userId = activity.userId,
                    activity = activity.toFirestore(),
                )
                dataRepository.markActivitySynced(activity.activityId, firestoreId)
                Timber.d("ActivityUploadWorker: activity id=${activity.activityId} uploaded successfully")

            } catch (e: Exception) {
                if (e.isRetryable()) {
                    Timber.w("ActivityUploadWorker: transient failure for id=${activity.activityId} – will retry")
                    hasRetryableFailure = true
                    // Activity stays as PENDING_UPLOAD and will be picked up on the next run.
                } else {
                    Timber.w("ActivityUploadWorker: permanent failure for id=${activity.activityId}")
                    dataRepository.markActivitySyncError(activity.activityId, e.message)
                }
            }
        }

        if (hasRetryableFailure) Result.retry() else Result.success()
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Returns `true` for transient network / server-unavailable errors that
     * are worth retrying.  All other exceptions are treated as permanent failures.
     */
    private fun Exception.isRetryable(): Boolean =
        this is IOException ||
        (this is FirebaseFirestoreException &&
            code == FirebaseFirestoreException.Code.UNAVAILABLE)

    companion object {
        /**
         * Builds an expedited one-time work request for immediate activity upload.
         * Requires a network connection ([SyncConstraints]).
         */
        fun startSyncWork() = OneTimeWorkRequestBuilder<DelegatingWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(SyncConstraints)
            .setInputData(ActivityUploadWorker::class.delegatedData())
            .build()
    }
}
