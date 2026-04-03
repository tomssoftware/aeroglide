package de.tomssoftware.aeroglide.sync.test

import de.tomssoftware.aeroglide.core.data.util.SyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * A [SyncManager] that never triggers a sync. Used in tests.
 */
internal class NeverSyncingSyncManager @Inject constructor() : SyncManager {
    override val isSyncing: Flow<Boolean> = flowOf(false)
    override fun requestSync() = Unit
}

