package de.tomssoftware.aeroglide.sync.test

import de.tomssoftware.aeroglide.core.data.util.SyncManager
import de.tomssoftware.aeroglide.sync.di.SyncModule
import de.tomssoftware.aeroglide.sync.status.StubSyncSubscriber
import de.tomssoftware.aeroglide.sync.status.SyncSubscriber
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

