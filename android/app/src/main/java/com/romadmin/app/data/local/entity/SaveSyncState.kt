package com.romadmin.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SyncStatus {
    IN_SYNC, LOCAL_NEWER, SERVER_NEWER, NEVER_SYNCED, SYNCING
}

@Entity(tableName = "save_sync_state")
data class SaveSyncState(
    @PrimaryKey val gameId: Int,
    val localSaveFileName: String? = null,
    val localTimestamp: Long? = null,
    val serverSaveId: Int? = null,
    val serverTimestamp: Long? = null,
    val lastSyncAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.NEVER_SYNCED,
)
