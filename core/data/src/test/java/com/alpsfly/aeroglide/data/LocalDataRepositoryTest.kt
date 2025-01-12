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

package com.alpsfly.aeroglide.data

import com.alpsfly.aeroglide.core.data.LocalDataRepository
import com.alpsfly.aeroglide.core.database.AltitudeDao
import com.alpsfly.aeroglide.core.database.CalibrationDao
import com.alpsfly.aeroglide.core.database.PressureDao
import com.alpsfly.aeroglide.core.database.SensorDataDao
import com.alpsfly.aeroglide.core.database.UserDao
import com.alpsfly.aeroglide.core.database.di.ClimbrateDao
import com.alpsfly.aeroglide.core.model.common.User
import com.alpsfly.aeroglide.core.model.database.Altitude
import com.alpsfly.aeroglide.core.model.database.Climbrate
import com.alpsfly.aeroglide.core.model.database.Pressure
import com.alpsfly.aeroglide.core.model.hardware.Calibration
import com.alpsfly.aeroglide.core.model.hardware.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [LocalDataRepository].
 */

class LocalDataRepositoryTest {

    @Test
    fun aeroGlides_newItemSaved_itemIsReturned() = runTest {
        val repository = LocalDataRepository(
            FakeAltitudeDao(),
            FakeCalibrationDao(),
            FakeClimbrateDao(),
            FakePressureDao(),
            FakeSensorDataDao(),
            FakeUserDao(),
        )

        repository.addUser(
            User(
                "1",
                "Test",
                "Test",
                "Test",
                "Test",
                "Test",
                "Test"
            )
        )

        // Todo: implement tests
        assertEquals(0, repository.users.first().size)
    }
}

private class FakeAltitudeDao : AltitudeDao {
    override fun getAllAltitude(): Flow<List<Altitude>> {
        TODO("Not yet implemented")
    }

    override fun addAltitude(altitude: Altitude) {
        TODO("Not yet implemented")
    }

}

private class FakeClimbrateDao : ClimbrateDao {
    override fun getAllClimbrate(): Flow<List<Climbrate>> {
        TODO("Not yet implemented")
    }

    override fun addClimbrate(climbrate: Climbrate) {
        TODO("Not yet implemented")
    }
}

private class FakeCalibrationDao : CalibrationDao {

    private val data = mutableListOf<Calibration>()
    private val _data = MutableStateFlow(mutableListOf<Calibration>())

    override fun getAllCalibration(): Flow<List<Calibration>> {
        return _data.asStateFlow()
    }

    override fun getLatestCalibration(): Flow<List<Calibration>> {
        return _data.asStateFlow()
    }

    override fun addCalibration(calibration: Calibration) {
        data.add(calibration)
    }
}

private class FakePressureDao : PressureDao {
    override fun getAllPressure(): Flow<List<Pressure>> {
        TODO("Not yet implemented")
    }

    override fun addPressure(pressure: Pressure) {
        TODO("Not yet implemented")
    }
}

private class FakeUserDao : UserDao {

    private val data = mutableListOf<User>()
    private val _data = MutableStateFlow(mutableListOf<User>())

    override fun getAllUsers(): Flow<List<User>> {
        return _data.asStateFlow()
    }

    override fun getUser(id: String): Flow<List<User>> {
        return _data.asStateFlow()
    }

    override fun addUser(user: User) {
        data.add(0, user)
    }

    override fun updateUser(user: User) {
        data[0].userId = user.userId
    }

    override fun removeAllUsers() {
        data.clear()
    }
}

private class FakeSensorDataDao : SensorDataDao {

    private val data = mutableListOf<SensorData>()
    private val _data = MutableStateFlow(mutableListOf<SensorData>())

    override fun getAllSensorData(): Flow<List<SensorData>> {
       return _data.asStateFlow()
    }

    override fun addSensorData(sensorData: SensorData) {
        data.add(SensorData())
    }
}
