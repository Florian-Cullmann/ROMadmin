package com.romadmin.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "romadmin_prefs")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.dataStore

    val serverUrl: Flow<String?> = dataStore.data.map { it[KEY_SERVER_URL] }
    val apiKey: Flow<String?> = dataStore.data.map { it[KEY_API_KEY] }
    val storageRoot: Flow<String?> = dataStore.data.map { it[KEY_STORAGE_ROOT] }
    val savesRootPath: Flow<String?> = dataStore.data.map { it[KEY_SAVES_ROOT] }
    val deviceName: Flow<String?> = dataStore.data.map { it[KEY_DEVICE_NAME] }
    val userId: Flow<Int?> = dataStore.data.map { it[KEY_USER_ID]?.toIntOrNull() }
    val username: Flow<String?> = dataStore.data.map { it[KEY_USERNAME] }

    val isSetupComplete: Flow<Boolean> = dataStore.data.map {
        !it[KEY_SERVER_URL].isNullOrBlank() &&
        !it[KEY_API_KEY].isNullOrBlank() &&
        !it[KEY_STORAGE_ROOT].isNullOrBlank()
    }

    suspend fun setServerUrl(url: String) {
        dataStore.edit { it[KEY_SERVER_URL] = url }
    }

    suspend fun setApiKey(key: String) {
        dataStore.edit { it[KEY_API_KEY] = key }
    }

    suspend fun setStorageRoot(path: String) {
        dataStore.edit { it[KEY_STORAGE_ROOT] = path }
    }

    suspend fun setSavesRootPath(path: String) {
        dataStore.edit { it[KEY_SAVES_ROOT] = path }
    }

    /**
     * Get the RetroArch core folder name for a platform.
     * Key format: "core_map_{platformFolderName}"
     */
    fun getCoreMapping(platformFolderName: String): Flow<String?> =
        dataStore.data.map { it[stringPreferencesKey("core_map_$platformFolderName")] }

    suspend fun setCoreMapping(platformFolderName: String, coreName: String) {
        dataStore.edit { it[stringPreferencesKey("core_map_$platformFolderName")] = coreName }
    }

    /**
     * Get resolved core folder for a platform, falling back to default mapping.
     */
    suspend fun getCoreFolderForPlatform(platformFolderName: String): String {
        val custom = getCoreMapping(platformFolderName).first()
        return custom ?: DEFAULT_CORE_MAP[platformFolderName] ?: platformFolderName
    }

    suspend fun setDeviceName(name: String) {
        dataStore.edit { it[KEY_DEVICE_NAME] = name }
    }

    suspend fun setUser(id: Int, username: String) {
        dataStore.edit {
            it[KEY_USER_ID] = id.toString()
            it[KEY_USERNAME] = username
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    companion object {
        private val KEY_SERVER_URL = stringPreferencesKey("server_url")
        private val KEY_API_KEY = stringPreferencesKey("api_key")
        private val KEY_STORAGE_ROOT = stringPreferencesKey("storage_root")
        private val KEY_SAVES_ROOT = stringPreferencesKey("saves_root")
        private val KEY_DEVICE_NAME = stringPreferencesKey("device_name")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USERNAME = stringPreferencesKey("username")

        /** Default mapping: platform folderName -> RetroArch core save folder */
        val DEFAULT_CORE_MAP = mapOf(
            "gb" to "Gambatte",
            "gbc" to "Gambatte",
            "gba" to "mGBA",
            "nes" to "Mesen",
            "snes" to "Snes9x",
            "n64" to "Mupen64Plus-Next",
            "nds" to "melonDS",
            "3ds" to "Citra",
            "ngc" to "dolphin-emu",
            "wii" to "dolphin-emu",
            "genesis" to "Genesis Plus GX",
            "psx" to "Beetle PSX HW",
            "ps2" to "PCSX2",
            "psp" to "PPSSPP",
        )
    }
}
