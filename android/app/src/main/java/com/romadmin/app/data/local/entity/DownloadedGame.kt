package com.romadmin.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DownloadStatus {
    PENDING, DOWNLOADING, COMPLETED, FAILED, PAUSED
}

@Entity(tableName = "downloaded_games")
data class DownloadedGame(
    @PrimaryKey val gameId: Int,
    val platformId: Int,
    val platformFolderName: String,
    val fileName: String,
    val displayName: String,
    val localPath: String,
    val fileSize: Long,
    val isDirectory: Boolean,
    val downloadedAt: Long = 0,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val bytesDownloaded: Long = 0,
    val thumbnailUrl: String? = null,
)
