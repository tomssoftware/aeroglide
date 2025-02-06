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
import com.alpsfly.aeroglide.core.database.AltitudeDao
import com.alpsfly.aeroglide.core.database.CalibrationDao
import com.alpsfly.aeroglide.core.database.PressureDao
import com.alpsfly.aeroglide.core.model.common.User
import com.alpsfly.aeroglide.core.database.UserDao
import com.alpsfly.aeroglide.core.database.ClimbrateDao
import com.alpsfly.aeroglide.core.database.LocationDao
import com.alpsfly.aeroglide.core.model.database.Activity
import com.alpsfly.aeroglide.core.model.database.Altitude
import com.alpsfly.aeroglide.core.model.database.Climbrate
import com.alpsfly.aeroglide.core.model.database.Location
import com.alpsfly.aeroglide.core.model.database.Pressure
import com.alpsfly.aeroglide.core.model.hardware.Calibration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

interface DataRepository {
    // Activity
    val allActivitiesFlow: Flow<List<Activity>>
    val activityFlow: Flow<Activity>
    fun activityFlow(activityId: Long): Flow<Activity>
    suspend fun getActivity(activityId: Long): Activity
    suspend fun addActivity(activity: Activity)
    suspend fun updateActivity(activity: Activity)

    // Altitude
    suspend fun addAltitude(altitude: Altitude)
    val altitude: Flow<List<Altitude>>
    val altitudeAsFlow: Flow<Altitude>

    // Calibration
    suspend fun addCalibration(calibration: Calibration)
    val calibration: Flow<List<Calibration>>

    // Climbrate
    suspend fun addClimbrate(climbrate: Climbrate)
    val climbrate: Flow<List<Climbrate>>

    // Pressure
    suspend fun addPressure(pressure: Pressure)
    val pressure: Flow<List<Pressure>>

    // Location
    suspend fun addLocation(location: Location)

    // User
    suspend fun addUser(user: User)
    val users: Flow<List<User>>

    // Other
    fun setActivityId(activityId: Long)
}

class LocalDataRepository @Inject constructor(
    private val activityDao: ActivityDao,
    private val altitudeDao: AltitudeDao,
    private val calibrationDao: CalibrationDao,
    private val climbrateDao: ClimbrateDao,
    private val locationDao: LocationDao,
    private val pressureDao: PressureDao,
    private val userDao: UserDao,
) : DataRepository {

    // Other
    private val _activityId = MutableStateFlow(0L)
    private val activityId = _activityId.asStateFlow()
    override fun setActivityId(activityId: Long) {
        _activityId.value = activityId
    }

    // Activity
    override val allActivitiesFlow: Flow<List<Activity>> = activityDao.allActivitiesFlow()
    @OptIn(ExperimentalCoroutinesApi::class)
    override val activityFlow: Flow<Activity> =
        activityId.filterNotNull().flatMapLatest { id ->
        activityDao.activityFlow(id)
    }

    override fun activityFlow(activityId: Long): Flow<Activity> = activityDao.activityFlow(activityId)
    override suspend fun getActivity(activityId: Long): Activity = activityDao.getActivity(activityId)
    override suspend fun addActivity(activity: Activity) = activityDao.addActivity(activity)
    override suspend fun updateActivity(activity: Activity) = activityDao.updateActivity(activity)

    // Altitude
    override suspend fun addAltitude(altitude: Altitude) = altitudeDao.addAltitude(altitude)
    override val altitude: Flow<List<Altitude>> = altitudeDao.getAllAltitude()
    override val altitudeAsFlow: Flow<Altitude> = altitudeDao.getAllAltitudeAsFlow()

    // Calibration
    override suspend fun addCalibration(calibration: Calibration) = calibrationDao.addCalibration(calibration)
    override val calibration: Flow<List<Calibration>> = calibrationDao.getLatestCalibration()

    // Climbrate
    override suspend fun addClimbrate(climbrate: Climbrate) = climbrateDao.addClimbrate(climbrate)
    override val climbrate: Flow<List<Climbrate>> = climbrateDao.getAllClimbrate()

    // Pressure
    override suspend fun addPressure(pressure: Pressure) = pressureDao.addPressure(pressure)
    override val pressure: Flow<List<Pressure>> = pressureDao.getAllPressure()

    // Location
    override suspend fun addLocation(location: Location) = locationDao.addLocation(location)

    // User
    override suspend fun addUser(user: User) = userDao.addUser(user)
    override val users: Flow<List<User>> = userDao.getAllUsers()
}