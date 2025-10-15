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
import com.alpsfly.aeroglide.core.database.ClimbrateDao
import com.alpsfly.aeroglide.core.database.LocationDao
import com.alpsfly.aeroglide.core.database.PressureDao
import com.alpsfly.aeroglide.core.database.UserDao
import com.alpsfly.aeroglide.core.model.common.User
import com.alpsfly.aeroglide.core.model.database.Activity
import com.alpsfly.aeroglide.core.model.database.Altitude
import com.alpsfly.aeroglide.core.model.database.Climbrate
import com.alpsfly.aeroglide.core.model.database.Location
import com.alpsfly.aeroglide.core.model.database.Pressure
import com.alpsfly.aeroglide.core.model.hardware.Calibration
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

    // Altitude
    suspend fun addAltitude(altitude: Altitude)
    val allAltitudes: Flow<List<Altitude>>
    fun getAltitude(timestamp: Long): Flow<Altitude?>
    fun getAltitudesBetween(start: Long, end: Long): Flow<List<Altitude>>

    // Calibration
    suspend fun addCalibration(calibration: Calibration)
    val calibration: Flow<List<Calibration>>

    // Climbrate
    suspend fun addClimbrate(climbrate: Climbrate)
    val allClimbrates: Flow<List<Climbrate>>
    fun getClimbrate(timestamp: Long): Flow<Climbrate?>
    fun getClimbratesBetween(start: Long, end: Long): Flow<List<Climbrate>>

    // Pressure
    suspend fun addPressure(pressure: Pressure)
    val pressure: Flow<List<Pressure>>

    // Location
    suspend fun addLocation(location: Location)
    val allLocations: Flow<List<Location>>
    fun getLocation(timestamp: Long): Flow<Location?>
    fun getLocationsBetween(start: Long, end: Long): Flow<List<Location>>

    // User
    suspend fun addUser(user: User)
    val users: Flow<List<User>>
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

    override val allActivities: Flow<List<Activity>> = activityDao.allActivitiesFlow()
    override fun getActivityFlow(activityId: Long): Flow<Activity?> = activityDao.getActivityFlow(activityId)
    override suspend fun getActivity(activityId: Long): Activity? = activityDao.getActivity(activityId)
    override suspend fun addActivity(activity: Activity) = activityDao.addActivity(activity)
    override suspend fun updateActivity(activity: Activity) = activityDao.updateActivity(activity)
    override suspend fun deleteActivity(activity: Activity) = activityDao.deleteActivity(activity)

    // Altitude
    override suspend fun addAltitude(altitude: Altitude) = altitudeDao.addAltitude(altitude)
    override val allAltitudes: Flow<List<Altitude>> = altitudeDao.allAltitudes
    override fun getAltitude(timestamp: Long): Flow<Altitude?> = altitudeDao.getAltitude(timestamp)
    override fun getAltitudesBetween(start: Long, end: Long) = altitudeDao.getAltitudesBetween(start, end)

    // Calibration
    override suspend fun addCalibration(calibration: Calibration) = calibrationDao.addCalibration(calibration)
    override val calibration: Flow<List<Calibration>> = calibrationDao.getLatestCalibration()

    // Climbrate
    override suspend fun addClimbrate(climbrate: Climbrate) = climbrateDao.addClimbrate(climbrate)
    override val allClimbrates: Flow<List<Climbrate>> = climbrateDao.allClimbrates
    override fun getClimbrate(timestamp: Long): Flow<Climbrate?> = climbrateDao.getClimbrate(timestamp)
    override fun getClimbratesBetween(start: Long, end: Long) = climbrateDao.getClimbratesBetween(start, end)

    // Pressure
    override suspend fun addPressure(pressure: Pressure) = pressureDao.addPressure(pressure)
    override val pressure: Flow<List<Pressure>> = pressureDao.getAllPressure()

    // Location
    override suspend fun addLocation(location: Location) = locationDao.addLocation(location)
    override val allLocations: Flow<List<Location>> = locationDao.allLocations
    override fun getLocation(timestamp: Long): Flow<Location?> = locationDao.getLocation(timestamp)
    override fun getLocationsBetween(start: Long, end: Long) = locationDao.getLocationsBetween(start, end)

    // User
    override suspend fun addUser(user: User) = userDao.addUser(user)
    override val users: Flow<List<User>> = userDao.getAllUsers()
}