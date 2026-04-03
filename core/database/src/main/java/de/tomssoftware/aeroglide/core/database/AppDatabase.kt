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

package de.tomssoftware.aeroglide.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import de.tomssoftware.aeroglide.core.model.database.User
import de.tomssoftware.aeroglide.core.model.database.Activity
import de.tomssoftware.aeroglide.core.model.database.TrackPoint
import de.tomssoftware.aeroglide.core.model.database.SyncState
import de.tomssoftware.aeroglide.core.model.database.Calibration

@Database(
    entities = [
        Activity::class,
        Calibration::class,
        TrackPoint::class,
        User::class,
    ],
    version = 1
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun calibrationDao(): CalibrationDao
    abstract fun trackDao(): TrackPointDao
    abstract fun userDao(): UserDao
}

class Converters {
    @TypeConverter
    fun fromSyncState(value: SyncState) = value.name

    @TypeConverter
    fun toSyncState(value: String) = SyncState.valueOf(value)
}
