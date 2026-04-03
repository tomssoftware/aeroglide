package com.alpsfly.aeroglide.sync.di

import com.alpsfly.aeroglide.core.data.util.SyncManager
import com.alpsfly.aeroglide.sync.status.FirebaseSyncSubscriber
import com.alpsfly.aeroglide.sync.status.SyncSubscriber
import com.alpsfly.aeroglide.sync.status.WorkManagerSyncManager
import com.google.firebase.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.messaging
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    internal abstract fun bindsSyncManager(
        syncStatusMonitor: WorkManagerSyncManager,
    ): SyncManager

    @Binds
    internal abstract fun bindsSyncSubscriber(
        syncSubscriber: FirebaseSyncSubscriber,
    ): SyncSubscriber

    companion object {
        @Provides
        @Singleton
        internal fun provideFirebaseMessaging(): FirebaseMessaging = Firebase.messaging
    }
}

