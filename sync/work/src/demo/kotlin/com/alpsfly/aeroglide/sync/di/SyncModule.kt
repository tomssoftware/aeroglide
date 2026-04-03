package com.alpsfly.aeroglide.sync.di

import com.alpsfly.aeroglide.core.data.util.SyncManager
import com.alpsfly.aeroglide.sync.status.StubSyncSubscriber
import com.alpsfly.aeroglide.sync.status.SyncSubscriber
import com.alpsfly.aeroglide.sync.status.WorkManagerSyncManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    internal abstract fun bindsSyncManager(
        syncStatusMonitor: WorkManagerSyncManager,
    ): SyncManager

    @Binds
    internal abstract fun bindsSyncSubscriber(
        syncSubscriber: StubSyncSubscriber,
    ): SyncSubscriber
}

