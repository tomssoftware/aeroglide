package com.alpsfly.aeroglide.core.common.di

import com.alpsfly.aeroglide.core.common.audio.VarioTone
import com.alpsfly.aeroglide.core.common.audio.VarioToneImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

/**
 * Hilt module that provides audio-related singletons for the application.
 *
 * Installed in [SingletonComponent] so that [VarioTone] survives Activity recreation.
 * A tone in progress will not be interrupted by screen rotation or configuration changes.
 */
@Module
@InstallIn(SingletonComponent::class)
object AudioModule {

    // Singleton: only one AudioTrack stream should be active app-wide at a time.
    // @ApplicationScope ensures playback coroutines are not tied to any Activity lifecycle
    // and continue running when the pilot navigates between screens mid-flight.
    @Provides
    @Singleton
    fun provideVarioTone(@ApplicationScope scope: CoroutineScope): VarioTone =
        VarioToneImpl(scope)
}

