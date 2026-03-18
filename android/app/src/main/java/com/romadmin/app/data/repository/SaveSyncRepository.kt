package com.romadmin.app.data.repository

import android.os.Build
import android.util.Log
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
        private const val TAG = "SaveSyncRepository"

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
     * Determine the timestamp to send to the server for sync comparison.
     *
     * The server compares our localTimestamp against its `uploadedAt` (server clock).
     * After an upload, `uploadedAt` is always later than the file's mtime because
     * the server records the time it received the file, not when RetroArch wrote it.
     *
     * To avoid a "download what you just uploaded" ping-pong:
     * - If the file hasn't changed since last sync → send the server's timestamp
     * - If the file IS newer than last sync → send the file's actual mtime
     */
    private fun getEffectiveTimestamp(fileMtime: Long, syncState: SaveSyncState?): String {
        if (syncState?.serverTimestamp != null && syncState.localTimestamp != null) {
            if (fileMtime <= syncState.localTimestamp) {
                // File unchanged since last sync — use server timestamp to stay "in_sync"
                return ISO_FORMAT.format(Date(syncState.serverTimestamp))
            }
        }
        // File is newer or no previous sync — send actual mtime
        return ISO_FORMAT.format(Date(fileMtime))
    }

    suspend fun syncSingleGame(gameId: Int, platformFolderName: String, romFileName: String): Boolean {
        val savesRoot = prefs.savesRootPath.first() ?: return false
        val deviceName = prefs.deviceName.first() ?: Build.MODEL
        val baseName = File(romFileName).nameWithoutExtension

        val saveDir = getSaveDir(platformFolderName) ?: return false
        val localSaveFile = findSaveFile(saveDir, baseName)
        val syncState = saveSyncDao.getByGameId(gameId)
        val localTimestamp = localSaveFile?.let {
            getEffectiveTimestamp(it.lastModified(), syncState)
        }

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
                val mtimeBefore = file.lastModified()
                delay(2000)
                if (file.lastModified() != mtimeBefore) return false
                val savedFile = uploadSave(gameId, file, deviceName)
                val serverTime = try {
                    ISO_FORMAT.parse(savedFile.uploadedAt)?.time ?: System.currentTimeMillis()
                } catch (_: Exception) { System.currentTimeMillis() }
                saveSyncDao.upsert(SaveSyncState(
                    gameId = gameId,
                    localSaveFileName = file.name,
                    localTimestamp = file.lastModified(),
                    serverSaveId = savedFile.id,
                    serverTimestamp = serverTime,
                    lastSyncAt = System.currentTimeMillis(),
                    syncStatus = SyncStatus.IN_SYNC,
                ))
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
                saveSyncDao.upsert(SaveSyncState(
                    gameId = gameId,
                    localSaveFileName = targetFile.name,
                    localTimestamp = targetFile.lastModified(),
                    serverSaveId = serverSave.id,
                    serverTimestamp = serverTime,
                    lastSyncAt = System.currentTimeMillis(),
                    syncStatus = SyncStatus.IN_SYNC,
                ))
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

    private suspend fun getSaveDir(platformFolderName: String): File? {
        val savesRoot = prefs.savesRootPath.first() ?: return null
        val coreFolder = prefs.getCoreFolderForPlatform(platformFolderName)
        return File(savesRoot, coreFolder)
    }

    suspend fun performSync(): Int {
        val savesRoot = prefs.savesRootPath.first() ?: return 0
        val deviceName = prefs.deviceName.first() ?: Build.MODEL

        val downloads = downloadDao.getByStatus(DownloadStatus.COMPLETED).first()
        if (downloads.isEmpty()) return 0

        // Scan for local save files
        val localSaves = mutableMapOf<Int, Pair<File, Long>>() // gameId -> (saveFile, mtime)
        for (download in downloads) {
            val saveDir = getSaveDir(download.platformFolderName) ?: continue
            val baseName = File(download.fileName).nameWithoutExtension
            val saveFile = findSaveFile(saveDir, baseName)
            if (saveFile != null) {
                localSaves[download.gameId] = Pair(saveFile, saveFile.lastModified())
            }
        }

        // Load previous sync states to get server timestamps
        val gameIds = downloads.map { it.gameId }
        val syncStates = saveSyncDao.getByGameIds(gameIds).associateBy { it.gameId }

        // Build sync status request using effective timestamps
        val gameEntries = downloads.map { download ->
            val localSave = localSaves[download.gameId]
            val syncState = syncStates[download.gameId]
            SyncGameEntry(
                gameId = download.gameId,
                localTimestamp = localSave?.let { getEffectiveTimestamp(it.second, syncState) },
            )
        }

        Log.d(TAG, "Syncing ${gameEntries.size} games, ${localSaves.size} with local saves")

        val response = api.syncStatus(SyncStatusRequest(deviceName, gameEntries))

        var syncCount = 0

        for (gameStatus in response.games) {
            when (gameStatus.action) {
                "upload", "no_server_save" -> {
                    val local = localSaves[gameStatus.gameId] ?: continue
                    val saveFile = local.first

                    val mtimeBefore = saveFile.lastModified()
                    delay(2000)
                    if (saveFile.lastModified() != mtimeBefore) {
                        Log.d(TAG, "Skipping game ${gameStatus.gameId}: file still being written")
                        continue
                    }

                    Log.d(TAG, "Uploading save for game ${gameStatus.gameId}: ${saveFile.name}")
                    val savedFile = uploadSave(gameStatus.gameId, saveFile, deviceName)
                    val serverTime = try {
                        ISO_FORMAT.parse(savedFile.uploadedAt)?.time ?: System.currentTimeMillis()
                    } catch (_: Exception) { System.currentTimeMillis() }
                    saveSyncDao.upsert(SaveSyncState(
                        gameId = gameStatus.gameId,
                        localSaveFileName = saveFile.name,
                        localTimestamp = saveFile.lastModified(),
                        serverSaveId = savedFile.id,
                        serverTimestamp = serverTime,
                        lastSyncAt = System.currentTimeMillis(),
                        syncStatus = SyncStatus.IN_SYNC,
                    ))
                    syncCount++
                }
                "download" -> {
                    val serverSave = gameStatus.serverSave ?: continue
                    val download = downloads.find { it.gameId == gameStatus.gameId } ?: continue

                    val saveDir = getSaveDir(download.platformFolderName) ?: continue
                    saveDir.mkdirs()
                    val baseName = File(download.fileName).nameWithoutExtension

                    Log.d(TAG, "Downloading save for game ${gameStatus.gameId}")
                    val dlResponse = api.downloadSave(serverSave.id)
                    val body = dlResponse.body() ?: continue

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

                    saveSyncDao.upsert(SaveSyncState(
                        gameId = gameStatus.gameId,
                        localSaveFileName = localSaveFile.name,
                        localTimestamp = localSaveFile.lastModified(),
                        serverSaveId = serverSave.id,
                        serverTimestamp = serverTime,
                        lastSyncAt = System.currentTimeMillis(),
                        syncStatus = SyncStatus.IN_SYNC,
                    ))
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

        Log.i(TAG, "Sync completed: $syncCount files synced")
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

    private suspend fun uploadSave(gameId: Int, file: File, deviceName: String): com.romadmin.app.domain.model.SaveFile {
        val requestFile = file.asRequestBody("application/octet-stream".toMediaType())
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("gameId", gameId.toString())
            .addFormDataPart("deviceName", deviceName)
            .addFormDataPart("file", file.name, requestFile)
            .build()

        return api.uploadSaveRaw(body)
    }
}
