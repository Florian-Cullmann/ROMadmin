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
import com.romadmin.app.domain.model.ServerSaveInfo
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
     * Parse the server's canonical timestamp for a save (clientTimestamp if available, else uploadedAt).
     * This is the timestamp the server uses for comparison, so the client must use the same value.
     */
    private fun parseServerCanonicalTime(serverSave: ServerSaveInfo): Long {
        // Prefer clientTimestamp — this is the file's actual mtime stored during upload.
        // Fall back to uploadedAt for saves uploaded before clientTimestamp was added.
        val tsString = serverSave.clientTimestamp ?: serverSave.uploadedAt
        return try {
            ISO_FORMAT.parse(tsString)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) { System.currentTimeMillis() }
    }

    suspend fun syncSingleGame(gameId: Int, platformFolderName: String, romFileName: String): Boolean {
        val savesRoot = prefs.savesRootPath.first() ?: return false
        val deviceName = prefs.deviceName.first() ?: Build.MODEL
        val baseName = File(romFileName).nameWithoutExtension

        val saveDirs = getSaveDirs(platformFolderName)
        val localSaveFile = findSaveFile(saveDirs, baseName)
        val localTimestamp = localSaveFile?.let {
            ISO_FORMAT.format(Date(it.lastModified()))
        }

        Log.d(TAG, "syncSingleGame gameId=$gameId baseName=$baseName " +
                "localFile=${localSaveFile?.absolutePath} localMtime=${localSaveFile?.lastModified()} localTimestamp=$localTimestamp")

        val request = SyncStatusRequest(deviceName, listOf(SyncGameEntry(gameId, localTimestamp)))
        val response = try {
            api.syncStatus(request)
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: "no body"
            throw Exception("Sync API error ${e.code()}: $errorBody")
        }

        val gameStatus = response.games.firstOrNull() ?: return false

        Log.d(TAG, "syncSingleGame gameId=$gameId action=${gameStatus.action} " +
                "serverSave=${gameStatus.serverSave?.id} serverClientTs=${gameStatus.serverSave?.clientTimestamp} " +
                "serverUploadedAt=${gameStatus.serverSave?.uploadedAt}")

        when (gameStatus.action) {
            "upload", "no_server_save" -> {
                val file = localSaveFile ?: return false
                val mtimeBefore = file.lastModified()
                delay(2000)
                if (file.lastModified() != mtimeBefore) {
                    Log.d(TAG, "Skipping gameId=$gameId: file mtime changed during stability check")
                    return false
                }
                Log.d(TAG, "Uploading save for gameId=$gameId: ${file.name} (${file.length()} bytes)")
                val savedFile = uploadSave(gameId, file, deviceName)
                saveSyncDao.upsert(SaveSyncState(
                    gameId = gameId,
                    localSaveFileName = file.name,
                    localTimestamp = file.lastModified(),
                    serverSaveId = savedFile.id,
                    serverTimestamp = file.lastModified(), // Server's clientTimestamp matches what we sent
                    lastSyncAt = System.currentTimeMillis(),
                    syncStatus = SyncStatus.IN_SYNC,
                ))
                return true
            }
            "download" -> {
                val serverSave = gameStatus.serverSave ?: return false
                val saveDir = getDownloadTargetDir(platformFolderName) ?: return false
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
                // Set file mtime to match the server's canonical timestamp so the next
                // sync-status comparison sees them as equal → in_sync (prevents ping-pong)
                val canonicalTime = parseServerCanonicalTime(serverSave)
                targetFile.setLastModified(canonicalTime)
                Log.d(TAG, "Downloaded save for gameId=$gameId, set mtime to $canonicalTime")
                saveSyncDao.upsert(SaveSyncState(
                    gameId = gameId,
                    localSaveFileName = targetFile.name,
                    localTimestamp = canonicalTime,
                    serverSaveId = serverSave.id,
                    serverTimestamp = canonicalTime,
                    lastSyncAt = System.currentTimeMillis(),
                    syncStatus = SyncStatus.IN_SYNC,
                ))
                return true
            }
            "in_sync" -> {
                if (localSaveFile != null) {
                    val canonicalTime = gameStatus.serverSave?.let { parseServerCanonicalTime(it) }
                    saveSyncDao.upsert(SaveSyncState(
                        gameId = gameId,
                        localSaveFileName = localSaveFile.name,
                        localTimestamp = localSaveFile.lastModified(),
                        serverSaveId = gameStatus.serverSave?.id,
                        serverTimestamp = canonicalTime,
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
     * Get all candidate save directories for a platform.
     * RetroArch may store saves in a core subfolder (e.g. saves/mGBA/) or
     * directly in the saves root (saves/) depending on user config.
     */
    private suspend fun getSaveDirs(platformFolderName: String): List<File> {
        val savesRoot = prefs.savesRootPath.first() ?: return emptyList()
        val coreFolder = prefs.getCoreFolderForPlatform(platformFolderName)
        return listOf(
            File(savesRoot, coreFolder),  // e.g. /RetroArch/saves/mGBA/
            File(savesRoot),              // e.g. /RetroArch/saves/
        )
    }

    /**
     * Get the best directory to write a downloaded save file into.
     * Prefer an existing core subfolder, fall back to saves root.
     */
    private suspend fun getDownloadTargetDir(platformFolderName: String): File? {
        val savesRoot = prefs.savesRootPath.first() ?: return null
        val coreFolder = prefs.getCoreFolderForPlatform(platformFolderName)
        val coreDir = File(savesRoot, coreFolder)
        // Use core subfolder if it already exists, otherwise use saves root
        return if (coreDir.isDirectory) coreDir else File(savesRoot)
    }

    suspend fun performSync(): Int {
        val savesRoot = prefs.savesRootPath.first() ?: return 0
        val deviceName = prefs.deviceName.first() ?: Build.MODEL

        val downloads = downloadDao.getByStatus(DownloadStatus.COMPLETED).first()
        if (downloads.isEmpty()) return 0

        // Scan for local save files
        val localSaves = mutableMapOf<Int, Pair<File, Long>>() // gameId -> (saveFile, mtime)
        for (download in downloads) {
            val saveDirs = getSaveDirs(download.platformFolderName)
            val baseName = File(download.fileName).nameWithoutExtension
            val saveFile = findSaveFile(saveDirs, baseName)
            if (saveFile != null) {
                localSaves[download.gameId] = Pair(saveFile, saveFile.lastModified())
            }
        }

        // Build sync status request — always send file's actual mtime.
        // Server compares against clientTimestamp (stored during upload) to avoid ping-pong.
        val gameEntries = downloads.map { download ->
            val localSave = localSaves[download.gameId]
            SyncGameEntry(
                gameId = download.gameId,
                localTimestamp = localSave?.let { ISO_FORMAT.format(Date(it.second)) },
            )
        }

        Log.d(TAG, "Syncing ${gameEntries.size} games, ${localSaves.size} with local saves")

        val response = api.syncStatus(SyncStatusRequest(deviceName, gameEntries))

        var syncCount = 0

        for (gameStatus in response.games) {
            Log.d(TAG, "performSync gameId=${gameStatus.gameId} action=${gameStatus.action} " +
                    "serverClientTs=${gameStatus.serverSave?.clientTimestamp} serverUploadedAt=${gameStatus.serverSave?.uploadedAt}")

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

                    Log.d(TAG, "Uploading save for game ${gameStatus.gameId}: ${saveFile.name} (${saveFile.length()} bytes)")
                    val savedFile = uploadSave(gameStatus.gameId, saveFile, deviceName)
                    saveSyncDao.upsert(SaveSyncState(
                        gameId = gameStatus.gameId,
                        localSaveFileName = saveFile.name,
                        localTimestamp = saveFile.lastModified(),
                        serverSaveId = savedFile.id,
                        serverTimestamp = saveFile.lastModified(), // Server's clientTimestamp matches what we sent
                        lastSyncAt = System.currentTimeMillis(),
                        syncStatus = SyncStatus.IN_SYNC,
                    ))
                    syncCount++
                }
                "download" -> {
                    val serverSave = gameStatus.serverSave ?: continue
                    val download = downloads.find { it.gameId == gameStatus.gameId } ?: continue

                    val saveDir = getDownloadTargetDir(download.platformFolderName) ?: continue
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

                    // Set file mtime to match the server's canonical timestamp to prevent ping-pong
                    val canonicalTime = parseServerCanonicalTime(serverSave)
                    localSaveFile.setLastModified(canonicalTime)
                    Log.d(TAG, "Downloaded save for game ${gameStatus.gameId}, set mtime to $canonicalTime")

                    saveSyncDao.upsert(SaveSyncState(
                        gameId = gameStatus.gameId,
                        localSaveFileName = localSaveFile.name,
                        localTimestamp = canonicalTime,
                        serverSaveId = serverSave.id,
                        serverTimestamp = canonicalTime,
                        lastSyncAt = System.currentTimeMillis(),
                        syncStatus = SyncStatus.IN_SYNC,
                    ))
                    syncCount++
                }
                "in_sync" -> {
                    val local = localSaves[gameStatus.gameId]
                    if (local != null) {
                        val canonicalTime = gameStatus.serverSave?.let { parseServerCanonicalTime(it) }
                        saveSyncDao.upsert(
                            SaveSyncState(
                                gameId = gameStatus.gameId,
                                localSaveFileName = local.first.name,
                                localTimestamp = local.second,
                                serverSaveId = gameStatus.serverSave?.id,
                                serverTimestamp = canonicalTime,
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

    /**
     * Search for the most recently modified save file across multiple directories.
     */
    private fun findSaveFile(dirs: List<File>, baseName: String): File? {
        var best: File? = null
        for (dir in dirs) {
            if (!dir.exists() || !dir.isDirectory) continue
            for (ext in SAVE_EXTENSIONS) {
                val candidate = File(dir, "$baseName$ext")
                if (candidate.exists()) {
                    if (best == null || candidate.lastModified() > best.lastModified()) {
                        best = candidate
                    }
                }
            }
        }
        return best
    }

    private suspend fun uploadSave(gameId: Int, file: File, deviceName: String): com.romadmin.app.domain.model.SaveFile {
        val requestFile = file.asRequestBody("application/octet-stream".toMediaType())
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("gameId", gameId.toString())
            .addFormDataPart("deviceName", deviceName)
            .addFormDataPart("clientTimestamp", ISO_FORMAT.format(Date(file.lastModified())))
            .addFormDataPart("file", file.name, requestFile)
            .build()

        return api.uploadSaveRaw(body)
    }
}
