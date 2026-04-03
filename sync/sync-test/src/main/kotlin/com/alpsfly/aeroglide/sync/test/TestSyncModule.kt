package com.alpsfly.aeroglide.sync.test

import com.alpsfly.aeroglide.core.data.util.SyncManager
import com.alpsfly.aeroglide.sync.di.SyncModule
import com.alpsfly.aeroglide.sync.status.StubSyncSubscriber
import com.alpsfly.aeroglide.sync.status.SyncSubscriber
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [SyncModule::class],
)
internal interface TestSyncModule {

    @Binds
    fun bindsSyncManager(
        syncStatusMonitor: NeverSyncingSyncManager,
    ): SyncManager

    @Binds
    fun bindsSyncSubscriber(
        syncSubscriber: StubSyncSubscriber,
    ): SyncSubscriber
}

