package com.romadmin.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.romadmin.app.data.local.dao.DownloadDao
import com.romadmin.app.data.local.dao.SaveSyncDao
import com.romadmin.app.data.local.entity.DownloadedGame
import com.romadmin.app.data.local.entity.SaveSyncState

@Database(
    entities = [DownloadedGame::class, SaveSyncState::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
    abstract fun saveSyncDao(): SaveSyncDao
}
