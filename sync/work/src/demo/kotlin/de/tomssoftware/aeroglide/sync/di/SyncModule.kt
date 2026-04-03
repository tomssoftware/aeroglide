package de.tomssoftware.aeroglide.sync.di

import de.tomssoftware.aeroglide.core.data.util.SyncManager
import de.tomssoftware.aeroglide.sync.status.StubSyncSubscriber
import de.tomssoftware.aeroglide.sync.status.SyncSubscriber
import de.tomssoftware.aeroglide.sync.status.WorkManagerSyncManager
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

