package com.alpsfly.aeroglide.di

import com.alpsfly.aeroglide.data.repository.LocationRepository
import com.alpsfly.aeroglide.data.repository.LocationRepositoryImpl
import com.alpsfly.aeroglide.data.repository.SensorRepository
import com.alpsfly.aeroglide.data.repository.SensorRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindSensorRepository(
        sensorRepositoryImpl: SensorRepositoryImpl
    ): SensorRepository

    @Binds
    @Singleton
    fun bindLocationRepository(
        locationRepositoryImpl: LocationRepositoryImpl
    ): LocationRepository
}