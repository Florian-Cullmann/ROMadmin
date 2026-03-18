package com.romadmin.app.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.romadmin.app.data.preferences.AppPreferences
import com.romadmin.app.data.repository.AuthRepository
import com.romadmin.app.data.repository.SaveSyncRepository
import com.romadmin.app.worker.SaveSyncWorker
import kotlinx.coroutines.flow.first
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed class ReAuthState {
    data object Idle : ReAuthState()
    data object Loading : ReAuthState()
    data object Success : ReAuthState()
    data class Error(val message: String) : ReAuthState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val prefs: AppPreferences,
    private val saveSyncRepository: SaveSyncRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val serverUrl = prefs.serverUrl.stateIn(viewModelScope, SharingStarted.Lazily, null)
    val username = prefs.username.stateIn(viewModelScope, SharingStarted.Lazily, null)
    val storageRoot = prefs.storageRoot.stateIn(viewModelScope, SharingStarted.Lazily, null)
    val savesRoot = prefs.savesRootPath.stateIn(viewModelScope, SharingStarted.Lazily, null)
    val deviceName = prefs.deviceName.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _reAuthState = MutableStateFlow<ReAuthState>(ReAuthState.Idle)
    val reAuthState = _reAuthState.asStateFlow()

    fun reAuthenticate(usernameInput: String, password: String) {
        viewModelScope.launch {
            _reAuthState.value = ReAuthState.Loading
            try {
                val url = prefs.serverUrl.first() ?: run {
                    _reAuthState.value = ReAuthState.Error("No server URL configured")
                    return@launch
                }
                val response = authRepository.login(url, usernameInput, password)
                val apiKey = authRepository.generateApiKey(url, response.accessToken)
                prefs.setApiKey(apiKey)
                prefs.setUser(response.user.id, response.user.username)
                _reAuthState.value = ReAuthState.Success
            } catch (e: Exception) {
                _reAuthState.value = ReAuthState.Error("Login failed: ${e.message}")
            }
        }
    }

    fun resetReAuthState() {
        _reAuthState.value = ReAuthState.Idle
    }

    fun updateDeviceName(name: String) {
        viewModelScope.launch { prefs.setDeviceName(name) }
    }

    fun updateStorageRoot(path: String) {
        viewModelScope.launch { prefs.setStorageRoot(path) }
    }

    fun updateSavesRoot(path: String) {
        viewModelScope.launch { prefs.setSavesRootPath(path) }
    }

    fun updateCoreMapping(platform: String, core: String) {
        viewModelScope.launch { prefs.setCoreMapping(platform, core) }
    }

    fun triggerSync() {
        val request = OneTimeWorkRequestBuilder<SaveSyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(SaveSyncWorker.WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage = _syncMessage.asStateFlow()

    private val _autoSyncEnabled = MutableStateFlow(false)
    val autoSyncEnabled = _autoSyncEnabled.asStateFlow()

    init {
        // Check if periodic sync is already enqueued
        viewModelScope.launch {
            val workInfos = WorkManager.getInstance(context)
                .getWorkInfosForUniqueWork(SaveSyncWorker.WORK_NAME_PERIODIC)
                .get()
            _autoSyncEnabled.value = workInfos.any { !it.state.isFinished }
        }
    }

    fun enablePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<SaveSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                SaveSyncWorker.WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        _autoSyncEnabled.value = true
        _syncMessage.value = "Auto-sync enabled (every 15 minutes)"
    }

    fun disablePeriodicSync() {
        WorkManager.getInstance(context).cancelUniqueWork(SaveSyncWorker.WORK_NAME_PERIODIC)
        _autoSyncEnabled.value = false
        _syncMessage.value = "Auto-sync disabled"
    }

    fun logout() {
        viewModelScope.launch {
            prefs.clear()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val serverUrl by viewModel.serverUrl.collectAsState()
    val username by viewModel.username.collectAsState()
    val storageRoot by viewModel.storageRoot.collectAsState()
    val savesRoot by viewModel.savesRoot.collectAsState()
    val deviceName by viewModel.deviceName.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Account section
            val reAuthState by viewModel.reAuthState.collectAsState()
            var showReAuthDialog by remember { mutableStateOf(false) }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Account", style = MaterialTheme.typography.titleMedium)
                    Text("Server: ${serverUrl ?: "Not set"}", style = MaterialTheme.typography.bodySmall)
                    Text("User: ${username ?: "Not set"}", style = MaterialTheme.typography.bodySmall)

                    OutlinedButton(
                        onClick = { showReAuthDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.Key, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Re-authenticate")
                    }
                }
            }

            if (showReAuthDialog) {
                var reAuthUser by remember { mutableStateOf(username ?: "") }
                var reAuthPass by remember { mutableStateOf("") }

                AlertDialog(
                    onDismissRequest = {
                        showReAuthDialog = false
                        viewModel.resetReAuthState()
                    },
                    title = { Text("Re-authenticate") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Sign in again to refresh your API key.", style = MaterialTheme.typography.bodySmall)
                            OutlinedTextField(
                                value = reAuthUser,
                                onValueChange = { reAuthUser = it },
                                label = { Text("Username") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedTextField(
                                value = reAuthPass,
                                onValueChange = { reAuthPass = it },
                                label = { Text("Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            when (val state = reAuthState) {
                                is ReAuthState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                is ReAuthState.Success -> {
                                    Text("Authenticated successfully!", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                                    LaunchedEffect(Unit) {
                                        kotlinx.coroutines.delay(1000)
                                        showReAuthDialog = false
                                        viewModel.resetReAuthState()
                                    }
                                }
                                else -> {}
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.reAuthenticate(reAuthUser, reAuthPass) },
                            enabled = reAuthState !is ReAuthState.Loading && reAuthUser.isNotBlank() && reAuthPass.isNotBlank(),
                        ) {
                            if (reAuthState is ReAuthState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Sign In")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showReAuthDialog = false
                            viewModel.resetReAuthState()
                        }) { Text("Cancel") }
                    },
                )
            }

            // Storage section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Storage", style = MaterialTheme.typography.titleMedium)
                    Text("ROMs: ${storageRoot ?: "Not set"}", style = MaterialTheme.typography.bodySmall)
                    Text("Saves: ${savesRoot ?: "Not set"}", style = MaterialTheme.typography.bodySmall)

                    var editingSaves by remember { mutableStateOf(false) }
                    var savesInput by remember(savesRoot) { mutableStateOf(savesRoot ?: "") }

                    if (editingSaves) {
                        OutlinedTextField(
                            value = savesInput,
                            onValueChange = { savesInput = it },
                            label = { Text("RetroArch Saves Path") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                viewModel.updateSavesRoot(savesInput)
                                editingSaves = false
                            }) { Text("Save") }
                            OutlinedButton(onClick = { editingSaves = false }) { Text("Cancel") }
                        }
                    } else {
                        OutlinedButton(onClick = { editingSaves = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Change Saves Path")
                        }
                    }
                }
            }

            // Core mapping section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Emulator Core Mapping", style = MaterialTheme.typography.titleMedium)
                    Text("Maps each platform to its RetroArch core save folder", style = MaterialTheme.typography.bodySmall)

                    com.romadmin.app.data.preferences.AppPreferences.DEFAULT_CORE_MAP.forEach { (platform, defaultCore) ->
                        var editing by remember { mutableStateOf(false) }
                        var coreInput by remember { mutableStateOf(defaultCore) }

                        if (editing) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(platform, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(48.dp))
                                OutlinedTextField(
                                    value = coreInput,
                                    onValueChange = { coreInput = it },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(onClick = {
                                    viewModel.updateCoreMapping(platform, coreInput)
                                    editing = false
                                }) { Icon(Icons.Default.Check, "Save", modifier = Modifier.size(18.dp)) }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { editing = true },
                            ) {
                                Text(platform, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(48.dp))
                                Text(defaultCore, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Device section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Device", style = MaterialTheme.typography.titleMedium)
                    var editingName by remember { mutableStateOf(false) }
                    var nameInput by remember(deviceName) { mutableStateOf(deviceName ?: "") }

                    if (editingName) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Device Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                viewModel.updateDeviceName(nameInput)
                                editingName = false
                            }) { Text("Save") }
                            OutlinedButton(onClick = { editingName = false }) { Text("Cancel") }
                        }
                    } else {
                        Row {
                            Text("Name: ${deviceName ?: "Not set"}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            IconButton(onClick = { editingName = true }) {
                                Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Save sync section
            val syncMsg by viewModel.syncMessage.collectAsState()
            val autoSyncOn by viewModel.autoSyncEnabled.collectAsState()

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Save Sync", style = MaterialTheme.typography.titleMedium)
                    Text("Automatically syncs save files between your device and the server", style = MaterialTheme.typography.bodySmall)

                    syncMsg?.let { msg ->
                        Text(msg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }

                    Button(onClick = viewModel::triggerSync, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Sync, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sync Now")
                    }

                    if (autoSyncOn) {
                        OutlinedButton(onClick = viewModel::disablePeriodicSync, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.SyncDisabled, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Disable Auto-Sync")
                        }
                    } else {
                        OutlinedButton(onClick = viewModel::enablePeriodicSync, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Schedule, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Enable Auto-Sync (15 min)")
                        }
                    }

                    // Battery optimization
                    OutlinedButton(
                        onClick = {
                            val pm = context.getSystemService(PowerManager::class.java)
                            if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.BatteryChargingFull, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Disable Battery Optimization")
                    }
                }
            }

            // Logout
            OutlinedButton(
                onClick = viewModel::logout,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}
