package com.romadmin.app.data.repository

import android.content.Context
import androidx.work.*
import com.romadmin.app.data.local.dao.DownloadDao
import com.romadmin.app.data.local.entity.DownloadStatus
import com.romadmin.app.data.local.entity.DownloadedGame
import com.romadmin.app.data.preferences.AppPreferences
import com.romadmin.app.data.remote.RomAdminApi
import com.romadmin.app.domain.model.ManifestGame
import com.romadmin.app.worker.DownloadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: RomAdminApi,
    private val downloadDao: DownloadDao,
    private val prefs: AppPreferences,
) {
    fun getAllDownloads(): Flow<List<DownloadedGame>> = downloadDao.getAllDownloads()

    fun getActiveDownloads(): Flow<List<DownloadedGame>> =
        downloadDao.getByStatuses(listOf(DownloadStatus.PENDING, DownloadStatus.DOWNLOADING))

    fun getCompletedDownloads(): Flow<List<DownloadedGame>> =
        downloadDao.getByStatus(DownloadStatus.COMPLETED)

    fun observeDownload(gameId: Int): Flow<DownloadedGame?> =
        downloadDao.observeByGameId(gameId)

    suspend fun getDownload(gameId: Int): DownloadedGame? =
        downloadDao.getByGameId(gameId)

    suspend fun enqueueDownload(
        gameId: Int,
        platformId: Int,
        platformFolderName: String,
        fileName: String,
        displayName: String,
        fileSize: Long,
        isDirectory: Boolean,
        thumbnailUrl: String?,
    ) {
        val storageRoot = prefs.storageRoot.first() ?: return
        val localPath = "$storageRoot/$platformFolderName/$fileName"

        // Create directory structure
        File(storageRoot, platformFolderName).mkdirs()

        val download = DownloadedGame(
            gameId = gameId,
            platformId = platformId,
            platformFolderName = platformFolderName,
            fileName = fileName,
            displayName = displayName,
            localPath = localPath,
            fileSize = fileSize,
            isDirectory = isDirectory,
            status = DownloadStatus.PENDING,
            thumbnailUrl = thumbnailUrl,
        )
        downloadDao.upsert(download)

        val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(workDataOf("gameId" to gameId))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag("download_game_$gameId")
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "download_game_$gameId",
                ExistingWorkPolicy.KEEP,
                workRequest,
            )
    }

    suspend fun enqueuePlatformDownload(platformId: Int, platformFolderName: String, games: List<ManifestGame>) {
        for (game in games) {
            val existing = downloadDao.getByGameId(game.id)
            if (existing?.status == DownloadStatus.COMPLETED) continue

            enqueueDownload(
                gameId = game.id,
                platformId = platformId,
                platformFolderName = platformFolderName,
                fileName = game.fileName,
                displayName = game.displayName,
                fileSize = game.fileSize.toLongOrNull() ?: 0L,
                isDirectory = game.isDirectory,
                thumbnailUrl = game.thumbnailUrl,
            )
        }
    }

    suspend fun cancelDownload(gameId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork("download_game_$gameId")
        downloadDao.updateProgress(gameId, DownloadStatus.PAUSED, 0)
    }

    suspend fun deleteDownload(gameId: Int) {
        val download = downloadDao.getByGameId(gameId) ?: return
        WorkManager.getInstance(context).cancelUniqueWork("download_game_$gameId")

        // Delete the file from disk
        val file = File(download.localPath)
        if (file.exists()) {
            if (file.isDirectory) file.deleteRecursively() else file.delete()
        }

        downloadDao.deleteByGameId(gameId)
    }
}
