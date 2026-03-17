package com.romadmin.app.worker

import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.romadmin.app.R
import com.romadmin.app.RomAdminApp
import com.romadmin.app.data.local.dao.DownloadDao
import com.romadmin.app.data.local.entity.DownloadStatus
import com.romadmin.app.data.remote.RomAdminApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val api: RomAdminApi,
    private val downloadDao: DownloadDao,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val gameId = inputData.getInt("gameId", -1)
        if (gameId == -1) return Result.failure()

        val download = downloadDao.getByGameId(gameId) ?: return Result.failure()

        // Show foreground notification (may fail if notification permission not granted)
        try {
            setForeground(createForegroundInfo(download.displayName, 0))
        } catch (_: Exception) {
            // Continue without foreground notification
        }

        downloadDao.updateProgress(gameId, DownloadStatus.DOWNLOADING, download.bytesDownloaded)

        return try {
            if (download.isDirectory) {
                downloadDirectoryGame(gameId, download.localPath, download.displayName)
            } else {
                downloadFileGame(gameId, download.localPath, download.fileSize, download.bytesDownloaded, download.displayName)
            }
            downloadDao.markCompleted(gameId)
            cancelNotification(gameId)
            Result.success()
        } catch (e: Exception) {
            downloadDao.updateProgress(gameId, DownloadStatus.FAILED, download.bytesDownloaded)
            cancelNotification(gameId)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private suspend fun downloadFileGame(
        gameId: Int,
        localPath: String,
        totalSize: Long,
        existingBytes: Long,
        displayName: String,
    ) {
        val file = File(localPath)
        file.parentFile?.mkdirs()

        // Check if we can resume
        val startByte = if (file.exists() && existingBytes > 0) existingBytes else 0L
        val rangeHeader = if (startByte > 0) "bytes=$startByte-" else null

        val response = api.downloadGame(gameId, rangeHeader)
        val body = response.body() ?: throw Exception("Empty response body")

        val outputStream = if (startByte > 0) {
            FileOutputStream(file, true) // append mode
        } else {
            FileOutputStream(file)
        }

        var bytesDownloaded = startByte
        val buffer = ByteArray(65536)
        var lastProgressUpdate = System.currentTimeMillis()

        body.byteStream().use { input ->
            outputStream.use { output ->
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                    bytesDownloaded += read

                    // Update progress every 500ms
                    val now = System.currentTimeMillis()
                    if (now - lastProgressUpdate > 500) {
                        downloadDao.updateProgress(gameId, DownloadStatus.DOWNLOADING, bytesDownloaded)
                        val percent = if (totalSize > 0) ((bytesDownloaded * 100) / totalSize).toInt() else 0
                        try { setForeground(createForegroundInfo(displayName, percent)) } catch (_: Exception) {}
                        lastProgressUpdate = now
                    }
                }
            }
        }

        downloadDao.updateProgress(gameId, DownloadStatus.DOWNLOADING, bytesDownloaded)
    }

    private suspend fun downloadDirectoryGame(
        gameId: Int,
        localPath: String,
        displayName: String,
    ) {
        val targetDir = File(localPath)
        targetDir.parentFile?.mkdirs()

        // Download as tar
        val response = api.downloadGame(gameId, null)
        val body = response.body() ?: throw Exception("Empty response body")

        // Extract tar directly to target
        targetDir.mkdirs()
        var bytesExtracted = 0L
        var lastProgressUpdate = System.currentTimeMillis()

        TarArchiveInputStream(BufferedInputStream(body.byteStream())).use { tar ->
            var entry = tar.nextEntry
            while (entry != null) {
                val outFile = File(targetDir, entry.name)

                // Prevent zip-slip
                if (!outFile.canonicalPath.startsWith(targetDir.canonicalPath)) {
                    throw SecurityException("Tar entry outside target dir: ${entry.name}")
                }

                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { output ->
                        val buffer = ByteArray(65536)
                        var read: Int
                        while (tar.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            bytesExtracted += read
                        }
                    }
                }

                val now = System.currentTimeMillis()
                if (now - lastProgressUpdate > 500) {
                    downloadDao.updateProgress(gameId, DownloadStatus.DOWNLOADING, bytesExtracted)
                    try { setForeground(createForegroundInfo(displayName, -1)) } catch (_: Exception) {}
                    lastProgressUpdate = now
                }

                entry = tar.nextEntry
            }
        }
    }

    private fun createForegroundInfo(title: String, percent: Int): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, RomAdminApp.CHANNEL_DOWNLOADS)
            .setContentTitle("Downloading")
            .setContentText(title)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .apply {
                if (percent >= 0) {
                    setProgress(100, percent, false)
                } else {
                    setProgress(0, 0, true)
                }
            }
            .build()

        return ForegroundInfo(
            NOTIFICATION_ID_BASE + inputData.getInt("gameId", 0),
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )
    }

    private fun cancelNotification(gameId: Int) {
        val nm = applicationContext.getSystemService(NotificationManager::class.java)
        nm.cancel(NOTIFICATION_ID_BASE + gameId)
    }

    companion object {
        private const val NOTIFICATION_ID_BASE = 10000
    }
}
