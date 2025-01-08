package com.alpsfly.aeroglide.core.common.di

import com.alpsfly.aeroglide.core.common.AndroidTimeProvider
import com.alpsfly.aeroglide.core.common.SystemTimeProvider
import com.alpsfly.aeroglide.core.common.TimeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Qualifier
import jakarta.inject.Singleton


@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class SystemTime

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class AndroidTime

@Module
@InstallIn(SingletonComponent::class)
object TimeProviderModule {
    @Provides
    //@Singleton
    @SystemTime
    fun provideSystemTimeProvider(): TimeProvider {
        return SystemTimeProvider()
    }

    @Provides
    //@Singleton
    @AndroidTime
    fun provideAndroidTimeProvider(): TimeProvider {
        return AndroidTimeProvider()
    }
}