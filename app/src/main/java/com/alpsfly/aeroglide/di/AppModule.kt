package com.alpsfly.aeroglide.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.alpsfly.aeroglide.AeroGlideDatabase
import com.alpsfly.aeroglide.AeroGlideRepository
import com.alpsfly.aeroglide.IAeroGlideRepository
import com.alpsfly.aeroglide.core.data.database.ILocalDataSource
import com.alpsfly.aeroglide.core.data.database.LocalDataSource
import com.alpsfly.aeroglide.core.data.model.local.TrackDao
import com.alpsfly.aeroglide.core.data.network.IWebDataSource
import com.alpsfly.aeroglide.core.data.network.WebDataSource
import com.alpsfly.aeroglide.core.data.repository.SensorRepository
import com.alpsfly.aeroglide.core.data.repository.SensorRepositoryImpl
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AeroGlideDatabase {
        return Room.databaseBuilder(
            appContext,
            AeroGlideDatabase::class.java,
            "aeroglide_database"
        ).build()
    }

    @Provides
    fun provideUserDao(db: AeroGlideDatabase): TrackDao {
        return db.trackDao()
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

@Module
@InstallIn(SingletonComponent::class)
interface AeroGlideRepositoryModule {
    @Binds
    @Singleton
    fun bindAeroGlideRepository(
        aeroGlideRepository: AeroGlideRepository
    ): IAeroGlideRepository
}

@Module
@InstallIn(SingletonComponent::class)
interface LocalDataSourceModule {
    @Binds
    @Singleton
    fun bindLocalDataSource(
        localDataSource: LocalDataSource
    ): ILocalDataSource
}

@Module
@InstallIn(SingletonComponent::class)
interface WebDataSourceModule {
    @Binds
    @Singleton
    fun bindWebDataSource(
        webDataSource: WebDataSource
    ): IWebDataSource
}