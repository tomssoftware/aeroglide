package com.alpsfly.aeroglide.core.data.di

import com.alpsfly.aeroglide.core.data.repository.SensorRepository
import com.alpsfly.aeroglide.core.data.repository.SensorRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// todo: check how to bind here an not in AppModule
//@Module
//@InstallIn(SingletonComponent::class)
//interface RepositoryModule {
//    @Binds
//    @Singleton
//    fun bindSensorRepository(
//        sensorRepositoryImpl: SensorRepositoryImpl
//    ): SensorRepository
//}