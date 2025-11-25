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

import android.content.Context
import android.hardware.SensorManager
import android.location.LocationManager
import com.alpsfly.aeroglide.core.data.AppRepository
import com.alpsfly.aeroglide.core.data.AppRepositoryImpl
import com.alpsfly.aeroglide.core.data.AutoStartSettingsProvider
import com.alpsfly.aeroglide.core.data.AutoStartSettingsProviderImpl
import com.alpsfly.aeroglide.core.data.BillingRepository
import com.alpsfly.aeroglide.core.data.BillingRepositoryImpl
import com.alpsfly.aeroglide.core.data.DataRepository
import com.alpsfly.aeroglide.core.data.ElevationRepository
import com.alpsfly.aeroglide.core.data.ElevationRepositoryImpl
import com.alpsfly.aeroglide.core.data.LocalDataRepository
import com.alpsfly.aeroglide.core.data.SensorRepository
import com.alpsfly.aeroglide.core.data.SensorRepositoryImpl
import com.alpsfly.aeroglide.core.model.common.User
import com.alpsfly.aeroglide.core.model.database.Activity
import com.alpsfly.aeroglide.core.model.database.Altitude
import com.alpsfly.aeroglide.core.model.database.Climbrate
import com.alpsfly.aeroglide.core.model.database.GlideRatio
import com.alpsfly.aeroglide.core.model.database.Location
import com.alpsfly.aeroglide.core.model.database.Pressure
import com.alpsfly.aeroglide.core.model.hardware.Calibration
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object SensorManagerModule {
    @Provides
    @Singleton
    fun provideSensorManager(@ApplicationContext context: Context): SensorManager {
        return context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
}

@Module
@InstallIn(SingletonComponent::class)
object LocationManagerModule {
    @Provides
    @Singleton
    fun provideLocationManager(@ApplicationContext context: Context): LocationManager {
        return context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
}

@Module
@InstallIn(SingletonComponent::class)
interface AppRepositoryModule {
    @Singleton
    @Binds
    fun bindsAppRepository(
        appRepository: AppRepositoryImpl
    ): AppRepository
}

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds
    @Singleton
    fun bindSensorRepository(
        sensorRepositoryImpl: SensorRepositoryImpl
    ): SensorRepository
}

@Module
@InstallIn(SingletonComponent::class)
interface DataRepositoryModule {
    @Singleton
    @Binds
    fun bindsDataRepository(
        localDataRepository: LocalDataRepository
    ): DataRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {
    @Binds
    abstract fun bindAutoStartSettingsProvider(
        impl: AutoStartSettingsProviderImpl
    ): AutoStartSettingsProvider
}

@Module
@InstallIn(SingletonComponent::class)
interface BillingRepositoryModule {
    @Binds
    @Singleton
    fun bindBillingRepository(impl: BillingRepositoryImpl): BillingRepository
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule { // It's good practice to create a separate module for network components

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            // You can add timeouts, interceptors, etc. here
            .build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
// Change this from an 'interface' to an 'object' to allow @Provides functions
object ElevationRepositoryModule {
    @Provides
    @Singleton
    fun provideElevationRepository(
        @ApplicationContext context: Context, // Explicitly ask Hilt for the ApplicationContext
        okHttpClient: OkHttpClient,
    ): ElevationRepository {
        // Manually construct the implementation. Hilt now knows exactly where
        // the context comes from and guarantees it is not null.
        return ElevationRepositoryImpl(context = context, okHttpClient = okHttpClient)
    }
}

class FakeDataRepository @Inject constructor(
    override val allActivities: Flow<List<Activity>>,
    override val allLocations: Flow<List<Location>>,
) : DataRepository {
    override val allAltitudes: Flow<List<Altitude>> = flowOf(fakeAltitude)
    override fun getAltitude(timestamp: Long): Flow<Altitude> {
        TODO("Not yet implemented")
    }

    override fun getAltitudesBetween(start: Long, end: Long): Flow<List<Altitude>> {
        TODO("Not yet implemented")
    }

    override val calibration: Flow<List<Calibration>> = flowOf(fakeCalibration)
    override val allClimbrates: Flow<List<Climbrate>> = flowOf(fakeClimbrate)
    override fun getClimbrate(timestamp: Long): Flow<Climbrate> {
        TODO("Not yet implemented")
    }

    override fun getClimbratesBetween(start: Long, end: Long): Flow<List<Climbrate>> {
        TODO("Not yet implemented")
    }

    override val pressure: Flow<List<Pressure>> = flowOf(fakePressure)
    override val users: Flow<List<User>> = flowOf(fakeUsers)


    override suspend fun addActivity(activity: Activity) {
        throw NotImplementedError()
    }

    override suspend fun addAltitude(altitude: Altitude) {
        throw NotImplementedError()
    }

    override suspend fun addClimbrate(climbrate: Climbrate) {
        throw NotImplementedError()
    }

    override suspend fun addPressure(pressure: Pressure) {
        throw NotImplementedError()
    }

    override suspend fun addUser(user: User) {
        throw NotImplementedError()
    }

    override fun getActivityFlow(activityId: Long): Flow<Activity> {
        TODO("Not yet implemented")
    }

    override suspend fun getActivity(activityId: Long): Activity {
        TODO("Not yet implemented")
    }

    override suspend fun updateActivity(activity: Activity) {
        throw NotImplementedError()
    }

    override suspend fun deleteActivity(activity: Activity) {
        TODO("Not yet implemented")
    }

    override suspend fun addCalibration(calibration: Calibration) {
        throw NotImplementedError()
    }

    override suspend fun addLocation(location: Location) {
        TODO("Not yet implemented")
    }

    override fun getLocation(timestamp: Long): Flow<Location> {
        TODO("Not yet implemented")
    }

    override fun getLocationsBetween(start: Long, end: Long): Flow<List<Location>> {
        TODO("Not yet implemented")
    }

    override fun getPressuresBetween(start: Long, end: Long): Flow<List<Pressure>> {
        TODO("Not yet implemented")
    }

    override fun getGlideRatiosBetween(start: Long, end: Long): Flow<List<GlideRatio>> {
        TODO("Not yet implemented")
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

val fakePressure = listOf(Pressure(), Pressure(), Pressure())
val fakeAltitude = listOf(Altitude(), Altitude(), Altitude())
val fakeClimbrate = listOf(Climbrate(), Climbrate(), Climbrate())

