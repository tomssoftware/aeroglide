package com.alpsfly.aeroglide.di

import com.alpsfly.aeroglide.data.repository.SensorRepository
import com.alpsfly.aeroglide.data.repository.SensorRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface SensorRepositoryModule {

    @Binds
    fun bindSensorRepository(
        sensorRepositoryImpl: SensorRepositoryImpl
    ): SensorRepository
}