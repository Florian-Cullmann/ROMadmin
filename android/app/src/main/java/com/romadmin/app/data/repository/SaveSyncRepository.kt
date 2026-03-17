package com.romadmin.app.data.repository

import android.os.Build
import com.romadmin.app.data.local.dao.DownloadDao
import com.romadmin.app.data.local.dao.SaveSyncDao
import com.romadmin.app.data.local.entity.DownloadStatus
import com.romadmin.app.data.local.entity.SaveSyncState
import com.romadmin.app.data.local.entity.SyncStatus
import com.romadmin.app.data.preferences.AppPreferences
import com.romadmin.app.data.remote.RomAdminApi
import com.romadmin.app.domain.model.SyncGameEntry
import com.romadmin.app.domain.model.SyncStatusRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaveSyncRepository @Inject constructor(
    private val api: RomAdminApi,
    private val downloadDao: DownloadDao,
    private val saveSyncDao: SaveSyncDao,
    private val prefs: AppPreferences,
) {
    companion object {
        private val SAVE_EXTENSIONS = listOf(
            ".srm", ".sav", ".sav0", ".sav1", ".sav2", ".sav3",
            ".sav4", ".sav5", ".sav6", ".sav7", ".sav8", ".sav9",
            ".state", ".ss0", ".ss1", ".ss2", ".ss3", ".ss4",
            ".ss5", ".ss6", ".ss7", ".ss8", ".ss9", ".oops",
            ".eep", ".fla", ".nds", ".dsv",
        )

        private val ISO_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    fun observeSyncState(gameId: Int): Flow<SaveSyncState?> = saveSyncDao.observeByGameId(gameId)

    /**
     * Sync a single game's save file by gameId.
     * Works regardless of whether the game was downloaded through the app —
     * only needs the RetroArch saves directory and the game's metadata from the server.
     */
    suspend fun syncSingleGame(gameId: Int, platformFolderName: String, romFileName: String): Boolean {
        val savesRoot = prefs.savesRootPath.first() ?: return false
        val deviceName = prefs.deviceName.first() ?: Build.MODEL
        val baseName = File(romFileName).nameWithoutExtension

        val saveDir = getSaveDir(platformFolderName) ?: return false
        val localSaveFile = findSaveFile(saveDir, baseName)
        val localTimestamp = localSaveFile?.let { ISO_FORMAT.format(Date(it.lastModified())) }

        val request = SyncStatusRequest(deviceName, listOf(SyncGameEntry(gameId, localTimestamp)))
        val response = try {
            api.syncStatus(request)
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: "no body"
            throw Exception("Sync API error ${e.code()}: $errorBody")
        }

        val gameStatus = response.games.firstOrNull() ?: return false

        when (gameStatus.action) {
            "upload", "no_server_save" -> {
                val file = localSaveFile ?: return false
                // Stability check
                val mtimeBefore = file.lastModified()
                delay(2000)
                if (file.lastModified() != mtimeBefore) return false
                uploadSave(gameId, file, deviceName)
                updateSyncState(gameId, file, file.lastModified())
                return true
            }
            "download" -> {
                val serverSave = gameStatus.serverSave ?: return false
                saveDir.mkdirs()
                val dlResponse = api.downloadSave(serverSave.id)
                val body = dlResponse.body() ?: return false
                val serverExt = serverSave.fileName.substringAfterLast('.', "srm")
                val targetFile = File(saveDir, "$baseName.$serverExt")
                targetFile.outputStream().use { output ->
                    body.byteStream().use { input ->
                        input.copyTo(output, bufferSize = 65536)
                    }
                }
                val serverTime = try {
                    ISO_FORMAT.parse(serverSave.uploadedAt)?.time ?: System.currentTimeMillis()
                } catch (_: Exception) { System.currentTimeMillis() }
                updateSyncState(gameId, targetFile, serverTime)
                return true
            }
            "in_sync" -> {
                if (localSaveFile != null) {
                    saveSyncDao.upsert(SaveSyncState(
                        gameId = gameId,
                        localSaveFileName = localSaveFile.name,
                        localTimestamp = localSaveFile.lastModified(),
                        serverSaveId = gameStatus.serverSave?.id,
                        serverTimestamp = gameStatus.serverSave?.let {
                            try { ISO_FORMAT.parse(it.uploadedAt)?.time } catch (_: Exception) { null }
                        },
                        lastSyncAt = System.currentTimeMillis(),
                        syncStatus = SyncStatus.IN_SYNC,
                    ))
                }
                return true
            }
        }
        return false
    }

    /**
     * Resolve the save directory for a given platform.
     * Structure: {savesRootPath}/{coreFolder}/
     * e.g. /sdcard/RetroArch/saves/mGBA/
     */
    private suspend fun getSaveDir(platformFolderName: String): File? {
        val savesRoot = prefs.savesRootPath.first() ?: return null
        val coreFolder = prefs.getCoreFolderForPlatform(platformFolderName)
        return File(savesRoot, coreFolder)
    }

    /**
     * Scan RetroArch save directories and sync with server.
     * Returns the number of files uploaded + downloaded.
     */
    suspend fun performSync(): Int {
        val savesRoot = prefs.savesRootPath.first() ?: return 0
        val deviceName = prefs.deviceName.first() ?: Build.MODEL

        // Get all completed downloads — those are the games we have locally
        val downloads = downloadDao.getByStatus(DownloadStatus.COMPLETED).first()
        if (downloads.isEmpty()) return 0

        // Scan for local save files in RetroArch saves directory
        val localSaves = mutableMapOf<Int, Pair<File, Long>>() // gameId -> (saveFile, mtime)
        for (download in downloads) {
            val saveDir = getSaveDir(download.platformFolderName) ?: continue
            val baseName = File(download.fileName).nameWithoutExtension

            // Look for save files in the core's save folder
            val saveFile = findSaveFile(saveDir, baseName)
            if (saveFile != null) {
                localSaves[download.gameId] = Pair(saveFile, saveFile.lastModified())
            }
        }

        // Build sync status request
        val gameEntries = downloads.map { download ->
            val localSave = localSaves[download.gameId]
            SyncGameEntry(
                gameId = download.gameId,
                localTimestamp = localSave?.let { ISO_FORMAT.format(Date(it.second)) },
            )
        }

        val response = api.syncStatus(SyncStatusRequest(deviceName, gameEntries))

        var syncCount = 0

        for (gameStatus in response.games) {
            when (gameStatus.action) {
                "upload", "no_server_save" -> {
                    val local = localSaves[gameStatus.gameId] ?: continue
                    val saveFile = local.first

                    // Stability check: wait 2s then verify mtime hasn't changed
                    val mtimeBefore = saveFile.lastModified()
                    delay(2000)
                    if (saveFile.lastModified() != mtimeBefore) continue // file is being written

                    uploadSave(gameStatus.gameId, saveFile, deviceName)
                    updateSyncState(gameStatus.gameId, saveFile, saveFile.lastModified())
                    syncCount++
                }
                "download" -> {
                    val serverSave = gameStatus.serverSave ?: continue
                    val download = downloads.find { it.gameId == gameStatus.gameId } ?: continue

                    // Resolve save directory for this platform's core
                    val saveDir = getSaveDir(download.platformFolderName) ?: continue
                    saveDir.mkdirs()
                    val baseName = File(download.fileName).nameWithoutExtension

                    // Download save from server
                    val dlResponse = api.downloadSave(serverSave.id)
                    val body = dlResponse.body() ?: continue

                    // Determine save extension from server filename
                    val serverExt = serverSave.fileName.substringAfterLast('.', "srm")
                    val localSaveFile = File(saveDir, "$baseName.$serverExt")

                    localSaveFile.outputStream().use { output ->
                        body.byteStream().use { input ->
                            input.copyTo(output, bufferSize = 65536)
                        }
                    }

                    val serverTime = try {
                        ISO_FORMAT.parse(serverSave.uploadedAt)?.time ?: System.currentTimeMillis()
                    } catch (_: Exception) {
                        System.currentTimeMillis()
                    }

                    updateSyncState(gameStatus.gameId, localSaveFile, serverTime)
                    syncCount++
                }
                "in_sync" -> {
                    val local = localSaves[gameStatus.gameId]
                    if (local != null) {
                        saveSyncDao.upsert(
                            SaveSyncState(
                                gameId = gameStatus.gameId,
                                localSaveFileName = local.first.name,
                                localTimestamp = local.second,
                                serverSaveId = gameStatus.serverSave?.id,
                                serverTimestamp = gameStatus.serverSave?.let {
                                    try { ISO_FORMAT.parse(it.uploadedAt)?.time } catch (_: Exception) { null }
                                },
                                lastSyncAt = System.currentTimeMillis(),
                                syncStatus = SyncStatus.IN_SYNC,
                            )
                        )
                    }
                }
            }
        }

        return syncCount
    }

    private fun findSaveFile(dir: File, baseName: String): File? {
        if (!dir.exists() || !dir.isDirectory) return null
        for (ext in SAVE_EXTENSIONS) {
            val candidate = File(dir, "$baseName$ext")
            if (candidate.exists()) return candidate
        }
        return null
    }

    private suspend fun uploadSave(gameId: Int, file: File, deviceName: String) {
        val requestFile = file.asRequestBody("application/octet-stream".toMediaType())
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("gameId", gameId.toString())
            .addFormDataPart("deviceName", deviceName)
            .addFormDataPart("file", file.name, requestFile)
            .build()

        api.uploadSaveRaw(body)
    }

    private suspend fun updateSyncState(gameId: Int, saveFile: File, timestamp: Long) {
        saveSyncDao.upsert(
            SaveSyncState(
                gameId = gameId,
                localSaveFileName = saveFile.name,
                localTimestamp = timestamp,
                serverSaveId = null,
                serverTimestamp = timestamp,
                lastSyncAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.IN_SYNC,
            )
        )
    }
}
