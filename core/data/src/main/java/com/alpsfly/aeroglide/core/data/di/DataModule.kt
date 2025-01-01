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

package com.alpsfly.aeroglide.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.LocalDataRepository
import com.alpsfly.aeroglide.core.model.common.User
import com.alpsfly.aeroglide.core.model.hardware.Calibration
import com.alpsfly.aeroglide.core.model.hardware.SensorData
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Singleton
    @Binds
    fun bindsAeroGlideRepository(
        aeroGlideRepository: LocalDataRepository
    ): DataRepository
}

class FakeDataRepository @Inject constructor() : DataRepository {
    override val users: Flow<List<User>> = flowOf(fakeUsers)
    override val calibration: Flow<List<Calibration>> = flowOf(fakeCalibration)
    override val sensorData: Flow<List<SensorData>> = flowOf(fakeSensorData)

    override suspend fun addUser(user: User) {
        throw NotImplementedError()
    }

    override suspend fun addCalibration(calibration: Calibration) {
        throw NotImplementedError()
    }

    override suspend fun addSensorData(sensorData: SensorData) {
        throw NotImplementedError()
    }
}

val fakeUsers = listOf(
    User("", "", "", "", "", "", ""),
    User("", "", "", "", "", "", ""),
    User("", "", "", "", "", "", "")
)

val fakeCalibration = listOf(
    Calibration(),
    Calibration(),
    Calibration()
)

val fakeSensorData = listOf(
    SensorData(),
    SensorData(),
    SensorData()
)
