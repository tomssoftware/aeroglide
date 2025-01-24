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
import com.alpsfly.aeroglide.core.database.SensorDataDao
import com.alpsfly.aeroglide.core.model.common.User
import com.alpsfly.aeroglide.core.database.UserDao
import com.alpsfly.aeroglide.core.database.ClimbrateDao
import com.alpsfly.aeroglide.core.model.database.Activity
import com.alpsfly.aeroglide.core.model.database.Altitude
import com.alpsfly.aeroglide.core.model.database.Climbrate
import com.alpsfly.aeroglide.core.model.database.Pressure
import com.alpsfly.aeroglide.core.model.hardware.Calibration
import com.alpsfly.aeroglide.core.model.hardware.SensorData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface DataRepository {
    suspend fun addActivity(activity: Activity)
    suspend fun addAltitude(altitude: Altitude)
    suspend fun addCalibration(calibration: Calibration)
    suspend fun addClimbrate(climbrate: Climbrate)
    suspend fun addPressure(pressure: Pressure)
    suspend fun addSensorData(sensorData: SensorData)
    suspend fun addUser(user: User)
    fun getActivity(activityId: Long): Flow<Activity>

    suspend fun updateActivity(activity: Activity)

    val allActivities: Flow<List<Activity>>
    val altitudeAsFlow: Flow<Altitude>
    val altitude: Flow<List<Altitude>>
    val calibration: Flow<List<Calibration>>
    val climbrate: Flow<List<Climbrate>>
    val pressure: Flow<List<Pressure>>
    val sensorData: Flow<List<SensorData>>
    val users: Flow<List<User>>
}

class LocalDataRepository @Inject constructor(
    private val activityDao: ActivityDao,
    private val altitudeDao: AltitudeDao,
    private val calibrationDao: CalibrationDao,
    private val climbrateDao: ClimbrateDao,
    private val pressureDao: PressureDao,
    private val sensorDataDao: SensorDataDao,
    private val userDao: UserDao,
) : DataRepository {

    override suspend fun addActivity(activity: Activity) {
        activityDao.addActivity(activity)
    }

    override suspend fun addAltitude(altitude: Altitude) {
        altitudeDao.addAltitude(altitude)
    }

    override suspend fun addCalibration(calibration: Calibration) {
        calibrationDao.addCalibration(calibration)
    }

    override suspend fun addClimbrate(climbrate: Climbrate) {
        climbrateDao.addClimbrate(climbrate)
    }

    override suspend fun addPressure(pressure: Pressure) {
        pressureDao.addPressure(pressure)
    }

    override suspend fun addSensorData(sensorData: SensorData) {
        sensorDataDao.addSensorData(sensorData)
    }

    override suspend fun addUser(user: User) {
        userDao.addUser(user)
    }

    override fun getActivity(activityId: Long): Flow<Activity> {
        return activityDao.getActivity(activityId)
    }

    override suspend fun updateActivity(activity: Activity) {
        activityDao.updateActivity(activity)
    }

    override val allActivities: Flow<List<Activity>> =
        activityDao.getAllActivity()

    override val altitudeAsFlow: Flow<Altitude> =
        altitudeDao.getAllAltitudeAsFlow()

    override val altitude: Flow<List<Altitude>> =
        altitudeDao.getAllAltitude()

    override val calibration: Flow<List<Calibration>> =
        calibrationDao.getLatestCalibration()

    override val climbrate: Flow<List<Climbrate>> =
        climbrateDao.getAllClimbrate()

    override val pressure: Flow<List<Pressure>> =
        pressureDao.getAllPressure()

    override val sensorData: Flow<List<SensorData>> =
        sensorDataDao.getAllSensorData()

    override val users: Flow<List<User>> =
        userDao.getAllUsers()
}
