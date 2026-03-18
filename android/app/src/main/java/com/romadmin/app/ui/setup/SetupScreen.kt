package com.romadmin.app.ui.setup

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.romadmin.app.data.preferences.AppPreferences
import com.romadmin.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SetupStep { SERVER, LOGIN, STORAGE, DONE }

data class SetupState(
    val step: SetupStep = SetupStep.SERVER,
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val storagePath: String = "${Environment.getExternalStorageDirectory().absolutePath}/Roms",
    val savesPath: String = "${Environment.getExternalStorageDirectory().absolutePath}/RetroArch/saves",
    val deviceName: String = Build.MODEL,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class SetupViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val authRepository: AuthRepository,
    private val prefs: AppPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(SetupState())
    val state = _state.asStateFlow()

    fun updateServerUrl(url: String) { _state.value = _state.value.copy(serverUrl = url, error = null) }
    fun updateUsername(u: String) { _state.value = _state.value.copy(username = u, error = null) }
    fun updatePassword(p: String) { _state.value = _state.value.copy(password = p, error = null) }
    fun updateStoragePath(p: String) { _state.value = _state.value.copy(storagePath = p, error = null) }
    fun updateSavesPath(p: String) { _state.value = _state.value.copy(savesPath = p, error = null) }
    fun updateDeviceName(n: String) { _state.value = _state.value.copy(deviceName = n, error = null) }

    fun testConnection() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val url = _state.value.serverUrl.trim()
                if (url.isBlank()) {
                    _state.value = _state.value.copy(isLoading = false, error = "Server URL is required")
                    return@launch
                }
                // Simple reachability check
                authRepository.testConnection(url)
                _state.value = _state.value.copy(isLoading = false, step = SetupStep.LOGIN)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "Cannot connect: ${e.message}")
            }
        }
    }

    fun login() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val s = _state.value
                val response = authRepository.login(
                    s.serverUrl.trim(), s.username.trim(), s.password, s.deviceName.trim()
                )
                _state.value = _state.value.copy(
                    isLoading = false,
                    step = SetupStep.STORAGE,
                )
                authRepository.saveSetup(
                    serverUrl = s.serverUrl.trim(),
                    token = response.token,
                    userId = response.user.id,
                    username = response.user.username,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "Login failed: ${e.message}")
            }
        }
    }

    fun verifyPaths() {
        val s = _state.value
        val romsDir = java.io.File(s.storagePath.trim())
        val savesDir = java.io.File(s.savesPath.trim())

        val errors = mutableListOf<String>()

        // ROMs path: create if it doesn't exist
        if (!romsDir.exists()) {
            val created = romsDir.mkdirs()
            if (!created) errors.add("Cannot create ROMs directory: ${s.storagePath}")
        }

        // Saves path: must already exist (RetroArch creates it)
        if (!savesDir.exists()) {
            errors.add("Saves directory not found: ${s.savesPath}\nMake sure RetroArch is installed and has been run at least once.")
        }

        if (errors.isNotEmpty()) {
            _state.value = _state.value.copy(error = errors.joinToString("\n\n"))
        } else {
            _state.value = _state.value.copy(error = null,
                // Show a green confirmation would be nice but let's just clear error
            )
        }
    }

    fun completeSetup() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val s = _state.value
                val dir = java.io.File(s.storagePath.trim())
                if (!dir.exists()) dir.mkdirs()

                val savesDir = java.io.File(s.savesPath.trim())
                if (!savesDir.exists()) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Saves directory not found: ${s.savesPath}\nMake sure RetroArch is installed."
                    )
                    return@launch
                }

                prefs.setStorageRoot(s.storagePath.trim())
                prefs.setSavesRootPath(s.savesPath.trim())
                prefs.setDeviceName(s.deviceName.trim())

                // Auto-enable periodic save sync
                val syncRequest = androidx.work.PeriodicWorkRequestBuilder<com.romadmin.app.worker.SaveSyncWorker>(
                    15, java.util.concurrent.TimeUnit.MINUTES
                ).setConstraints(
                    androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                        .build()
                ).build()
                androidx.work.WorkManager.getInstance(context)
                    .enqueueUniquePeriodicWork(
                        com.romadmin.app.worker.SaveSyncWorker.WORK_NAME_PERIODIC,
                        androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                        syncRequest,
                    )

                _state.value = _state.value.copy(isLoading = false, step = SetupStep.DONE)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "Failed: ${e.message}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    onSetupComplete: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.step) {
        if (state.step == SetupStep.DONE) {
            onSetupComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RomAdmin Setup") },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Step indicator
            LinearProgressIndicator(
                progress = { when (state.step) {
                    SetupStep.SERVER -> 0.25f
                    SetupStep.LOGIN -> 0.5f
                    SetupStep.STORAGE -> 0.75f
                    SetupStep.DONE -> 1f
                }},
                modifier = Modifier.fillMaxWidth(),
            )

            when (state.step) {
                SetupStep.SERVER -> ServerStep(state, viewModel)
                SetupStep.LOGIN -> LoginStep(state, viewModel)
                SetupStep.STORAGE -> StorageStep(state, viewModel)
                SetupStep.DONE -> {} // handled by LaunchedEffect
            }
        }
    }
}

@Composable
private fun ServerStep(state: SetupState, viewModel: SetupViewModel) {
    Text("Server Connection", style = MaterialTheme.typography.headlineSmall)
    Text("Enter the URL of your RomAdmin server", style = MaterialTheme.typography.bodyMedium)

    OutlinedTextField(
        value = state.serverUrl,
        onValueChange = viewModel::updateServerUrl,
        label = { Text("Server URL") },
        placeholder = { Text("http://192.168.1.100:3000") },
        leadingIcon = { Icon(Icons.Default.Dns, contentDescription = null) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        modifier = Modifier.fillMaxWidth(),
    )

    state.error?.let {
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }

    Button(
        onClick = viewModel::testConnection,
        enabled = !state.isLoading && state.serverUrl.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Text("Connect")
        }
    }
}

@Composable
private fun LoginStep(state: SetupState, viewModel: SetupViewModel) {
    Text("Login", style = MaterialTheme.typography.headlineSmall)
    Text("Sign in with your RomAdmin account", style = MaterialTheme.typography.bodyMedium)

    OutlinedTextField(
        value = state.username,
        onValueChange = viewModel::updateUsername,
        label = { Text("Username") },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )

    OutlinedTextField(
        value = state.password,
        onValueChange = viewModel::updatePassword,
        label = { Text("Password") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )

    state.error?.let {
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }

    Button(
        onClick = viewModel::login,
        enabled = !state.isLoading && state.username.isNotBlank() && state.password.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Text("Sign In")
        }
    }
}

@Composable
private fun StorageStep(state: SetupState, viewModel: SetupViewModel) {
    val context = LocalContext.current
    val hasStoragePermission = remember { mutableStateOf(Environment.isExternalStorageManager()) }

    Text("Storage", style = MaterialTheme.typography.headlineSmall)
    Text("Choose where to store downloaded ROMs", style = MaterialTheme.typography.bodyMedium)

    // Storage permission prompt
    if (!hasStoragePermission.value) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Storage Permission Required", style = MaterialTheme.typography.titleSmall)
                Text("RomAdmin needs access to manage files on your device to download ROMs and sync saves.", style = MaterialTheme.typography.bodySmall)
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                        // Re-check after returning (will update on recomposition)
                        hasStoragePermission.value = Environment.isExternalStorageManager()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Grant Permission")
                }
            }
        }
    }

    OutlinedTextField(
        value = state.storagePath,
        onValueChange = viewModel::updateStoragePath,
        label = { Text("ROMs Storage Path") },
        placeholder = { Text("/sdcard/Roms") },
        leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )

    OutlinedTextField(
        value = state.savesPath,
        onValueChange = viewModel::updateSavesPath,
        label = { Text("RetroArch Saves Path") },
        placeholder = { Text("/sdcard/RetroArch/saves") },
        leadingIcon = { Icon(Icons.Default.Save, contentDescription = null) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )

    Text(
        "Save files are synced from RetroArch/saves/{core}/ (e.g. mGBA, dolphin-emu)",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    OutlinedTextField(
        value = state.deviceName,
        onValueChange = viewModel::updateDeviceName,
        label = { Text("Device Name") },
        leadingIcon = { Icon(Icons.Default.Phonelink, contentDescription = null) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )

    Text(
        "Identifies this device when syncing saves",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    OutlinedButton(
        onClick = {
            hasStoragePermission.value = Environment.isExternalStorageManager()
            if (hasStoragePermission.value) viewModel.verifyPaths()
        },
        enabled = state.storagePath.isNotBlank() && state.savesPath.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Verify Paths")
    }

    state.error?.let {
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }

    if (state.error == null && state.storagePath.isNotBlank()) {
        // Only show after verify succeeds (error is cleared)
    }

    Button(
        onClick = {
            hasStoragePermission.value = Environment.isExternalStorageManager()
            if (hasStoragePermission.value) {
                viewModel.completeSetup()
            }
        },
        enabled = !state.isLoading && state.storagePath.isNotBlank() && state.savesPath.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Text("Complete Setup")
        }
    }
}
