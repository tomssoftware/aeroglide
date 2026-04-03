/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alpsfly.aeroglide.core.data

import com.alpsfly.aeroglide.core.database.ActivityDao
import com.alpsfly.aeroglide.core.database.CalibrationDao
import com.alpsfly.aeroglide.core.database.TrackPointDao
import com.alpsfly.aeroglide.core.database.UserDao
import com.alpsfly.aeroglide.core.model.database.User
import com.alpsfly.aeroglide.core.model.database.Activity
import com.alpsfly.aeroglide.core.model.database.SyncState
import com.alpsfly.aeroglide.core.model.database.TrackPoint
import com.alpsfly.aeroglide.core.model.database.Calibration
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface DataRepository {
    // Activity
    val allActivities: Flow<List<Activity>>
    fun getActivityFlow(activityId: Long): Flow<Activity?>
    suspend fun getActivity(activityId: Long): Activity?
    suspend fun addActivity(activity: Activity)
    suspend fun updateActivity(activity: Activity)
    suspend fun deleteActivity(activity: Activity)

    // Sync
    /** Returns all activities that still need to be uploaded (LOCAL + PENDING_UPLOAD). */
    suspend fun getPendingActivities(): List<Activity>
    /** Marks an activity as successfully uploaded; stores the Firestore document ID. */
    suspend fun markActivitySynced(activityId: Long, firestoreId: String)
    /** Marks an activity as picked up by the upload worker (prevents parallel processing). */
    suspend fun markActivityPendingUpload(activityId: Long)
    /** Marks an activity as failed; stores the error message for diagnostics. */
    suspend fun markActivitySyncError(activityId: Long, error: String?)

    // Calibration
    suspend fun addCalibration(calibration: Calibration)
    val calibration: Flow<List<Calibration>>

    // Track
    suspend fun addTrack(trackPoint: TrackPoint)
    fun getTracksBetween(start: Long, end: Long): Flow<List<TrackPoint>>

    // User
    suspend fun addUser(user: User)
    val users: Flow<List<User>>
}

class LocalDataRepository @Inject constructor(
    private val activityDao: ActivityDao,
    private val calibrationDao: CalibrationDao,
    private val trackPointDao: TrackPointDao,
    private val userDao: UserDao,
) : DataRepository {

    override val allActivities: Flow<List<Activity>> = activityDao.allActivitiesFlow()
    override fun getActivityFlow(activityId: Long): Flow<Activity?> = activityDao.getActivityFlow(activityId)
    override suspend fun getActivity(activityId: Long): Activity? = activityDao.getActivity(activityId)
    override suspend fun addActivity(activity: Activity) = activityDao.addActivity(activity)
    override suspend fun updateActivity(activity: Activity) = activityDao.updateActivity(activity)
    override suspend fun deleteActivity(activity: Activity) = activityDao.deleteActivity(activity)

    // Sync
    override suspend fun getPendingActivities(): List<Activity> =
        activityDao.getActivitiesBySyncState(SyncState.LOCAL) +
        activityDao.getActivitiesBySyncState(SyncState.PENDING_UPLOAD)

    override suspend fun markActivitySynced(activityId: Long, firestoreId: String) =
        activityDao.updateSyncState(activityId, SyncState.SYNCED, firestoreId, System.currentTimeMillis())

    override suspend fun markActivityPendingUpload(activityId: Long) =
        activityDao.updateSyncStateOnly(activityId, SyncState.PENDING_UPLOAD)

    override suspend fun markActivitySyncError(activityId: Long, error: String?) =
        activityDao.setSyncError(activityId, SyncState.ERROR, error)

    // Calibration
    override suspend fun addCalibration(calibration: Calibration) = calibrationDao.addCalibration(calibration)
    override val calibration: Flow<List<Calibration>> = calibrationDao.getLatestCalibration()

    // Track
    override suspend fun addTrack(trackPoint: TrackPoint) = trackPointDao.addTrackPoint(trackPoint)
    override fun getTracksBetween(start: Long, end: Long) = trackPointDao.getTrackPointsBetween(start, end)

    // User
    override suspend fun addUser(user: User) = userDao.addUser(user)
    override val users: Flow<List<User>> = userDao.getAllUsers()
}
