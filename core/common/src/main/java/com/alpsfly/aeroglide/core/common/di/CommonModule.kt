package com.alpsfly.aeroglide.core.common.di

import com.alpsfly.aeroglide.core.common.AndroidTimeProvider
import com.alpsfly.aeroglide.core.common.SystemTimeProvider
import com.alpsfly.aeroglide.core.common.TimeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Qualifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class SystemTime

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class AndroidTime

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {
    @Provides
    @DefaultDispatcher // Label this provider
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @IoDispatcher // Label this provider
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @MainDispatcher // Label this provider
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}

@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {

    @Singleton
    @ApplicationScope
    @Provides
    fun provideCoroutineScope(): CoroutineScope {
        // SupervisorJob() is used so that if one child coroutine fails, it doesn't cancel the whole scope.
        // Dispatchers.Default is a good choice for background work that is CPU-intensive.
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}

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