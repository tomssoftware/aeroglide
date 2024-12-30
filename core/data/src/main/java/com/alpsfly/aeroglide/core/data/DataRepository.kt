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

import com.alpsfly.aeroglide.core.database.CalibrationDao
import com.alpsfly.aeroglide.core.model.common.User
import com.alpsfly.aeroglide.core.database.UserDao
import com.alpsfly.aeroglide.core.model.hardware.Calibration
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface DataRepository {
    val users: Flow<List<User>>
    val calibration: Flow<List<Calibration>>

    suspend fun addUser(user: User)
    suspend fun addCalibration(calibration: Calibration)
}

class LocalDataRepository @Inject constructor(
    private val userDao: UserDao,
    private val calibrationDao: CalibrationDao
) : DataRepository {

    override val users: Flow<List<User>> =
        userDao.getAllUsers()

    override suspend fun addUser(user: User) {
        userDao.addUser(user)
    }

    override val calibration: Flow<List<Calibration>> =
        calibrationDao.getLatestCalibration()

    override suspend fun addCalibration(calibration: Calibration) {
        calibrationDao.addCalibration(calibration)
    }
}
