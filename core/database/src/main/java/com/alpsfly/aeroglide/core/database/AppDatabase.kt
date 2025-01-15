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

package com.alpsfly.aeroglide.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alpsfly.aeroglide.core.model.common.User
import com.alpsfly.aeroglide.core.model.database.Activity
import com.alpsfly.aeroglide.core.model.database.Altitude
import com.alpsfly.aeroglide.core.model.database.Climbrate
import com.alpsfly.aeroglide.core.model.database.Pressure
import com.alpsfly.aeroglide.core.model.hardware.Calibration
import com.alpsfly.aeroglide.core.model.hardware.SensorData

@Database(
    entities = [
        Activity::class,
        Altitude::class,
        Calibration::class,
        Climbrate::class,
        Pressure::class,
        SensorData::class,
        User::class,
    ],
    version = 1
)
@TypeConverters(FloatArrayTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun altitudeDao(): AltitudeDao
    abstract fun calibrationDao(): CalibrationDao
    abstract fun climbrateDao(): ClimbrateDao
    abstract fun pressureDao(): PressureDao
    abstract fun sensorDataDao(): SensorDataDao
    abstract fun userDao(): UserDao
}
