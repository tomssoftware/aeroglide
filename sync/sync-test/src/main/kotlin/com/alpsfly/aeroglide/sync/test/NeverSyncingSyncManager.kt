package com.alpsfly.aeroglide.sync.test

import com.alpsfly.aeroglide.core.data.util.SyncManager
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

