package com.alpsfly.aeroglide.di

import android.app.Application
import com.alpsfly.aeroglide.core.data.repository.SensorRepository
import com.alpsfly.aeroglide.core.data.repository.SensorRepositoryImpl
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(app: Application): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(app)
    }
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