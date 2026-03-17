package com.romadmin.app.ui.games

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.romadmin.app.data.local.dao.DownloadDao
import com.romadmin.app.data.local.dao.SaveSyncDao
import com.romadmin.app.data.local.entity.DownloadStatus
import com.romadmin.app.data.local.entity.DownloadedGame
import com.romadmin.app.data.local.entity.SaveSyncState
import com.romadmin.app.data.local.entity.SyncStatus
import com.romadmin.app.data.repository.DownloadRepository
import com.romadmin.app.data.repository.GameRepository
import com.romadmin.app.data.repository.SaveSyncRepository
import com.romadmin.app.domain.model.Game
import com.romadmin.app.util.formatFileSize
import com.romadmin.app.util.formatFileSizeFromString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class GameDetailState(
    val game: Game? = null,
    val download: DownloadedGame? = null,
    val syncState: SaveSyncState? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val syncMessage: String? = null,
    val isSyncing: Boolean = false,
)

@HiltViewModel
class GameDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gameRepository: GameRepository,
    private val downloadRepository: DownloadRepository,
    private val downloadDao: DownloadDao,
    private val saveSyncRepository: SaveSyncRepository,
    private val saveSyncDao: SaveSyncDao,
) : ViewModel() {

    private val gameId: Int = savedStateHandle["gameId"] ?: 0
    private val _state = MutableStateFlow(GameDetailState())
    val state = _state.asStateFlow()

    init {
        loadGame()
        observeDownload()
        observeSyncState()
    }

    private fun loadGame() {
        viewModelScope.launch {
            try {
                val game = gameRepository.getGame(gameId)
                _state.value = _state.value.copy(game = game, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun observeDownload() {
        viewModelScope.launch {
            downloadDao.observeByGameId(gameId).collect { dl ->
                _state.value = _state.value.copy(download = dl)
            }
        }
    }

    private fun observeSyncState() {
        viewModelScope.launch {
            saveSyncDao.observeByGameId(gameId).collect { sync ->
                _state.value = _state.value.copy(syncState = sync)
            }
        }
    }

    fun startDownload() {
        val game = _state.value.game ?: return
        viewModelScope.launch {
            downloadRepository.enqueueDownload(
                gameId = game.id,
                platformId = game.platformId,
                platformFolderName = game.platform?.folderName ?: "",
                fileName = game.fileName,
                displayName = game.displayName,
                fileSize = game.fileSize.toLongOrNull() ?: 0L,
                isDirectory = game.isDirectory,
                thumbnailUrl = game.thumbnailUrl,
            )
        }
    }

    fun cancelDownload() {
        viewModelScope.launch {
            downloadRepository.cancelDownload(gameId)
        }
    }

    fun deleteDownload() {
        viewModelScope.launch {
            downloadRepository.deleteDownload(gameId)
        }
    }

    fun syncSaves() {
        val game = _state.value.game ?: return
        _state.value = _state.value.copy(isSyncing = true, syncMessage = null)
        viewModelScope.launch {
            try {
                val result = saveSyncRepository.syncSingleGame(
                    gameId = game.id,
                    platformFolderName = game.platform?.folderName ?: "",
                    romFileName = game.fileName,
                )
                val syncState = saveSyncDao.getByGameId(gameId)
                _state.value = _state.value.copy(
                    syncState = syncState,
                    isSyncing = false,
                    syncMessage = if (result) "Sync completed" else "No save file found locally",
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSyncing = false,
                    syncMessage = "Sync failed: ${e.message}",
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    onBack: () -> Unit,
    viewModel: GameDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.game?.displayName ?: "Game") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val game = state.game ?: return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Cover image
            if (game.coverUrl != null) {
                AsyncImage(
                    model = game.coverUrl,
                    contentDescription = game.displayName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp),
                )
            }

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Title and metadata chips
                Text(game.displayName, style = MaterialTheme.typography.headlineSmall)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    game.platform?.let {
                        AssistChip(onClick = {}, label = { Text(it.displayName) })
                    }
                    AssistChip(onClick = {}, label = { Text(formatFileSizeFromString(game.fileSize)) })
                    game.releaseDate?.let { date ->
                        val year = try {
                            date.substring(0, 4)
                        } catch (_: Exception) { null }
                        year?.let {
                            AssistChip(onClick = {}, label = { Text(it) })
                        }
                    }
                }

                // Description
                game.description?.let {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(it, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // Download section
                DownloadSection(
                    download = state.download,
                    fileSize = game.fileSize.toLongOrNull() ?: 0L,
                    onDownload = viewModel::startDownload,
                    onCancel = viewModel::cancelDownload,
                    onDelete = viewModel::deleteDownload,
                )

                // Save sync section
                SaveSyncSection(
                    syncState = state.syncState,
                    isSyncing = state.isSyncing,
                    syncMessage = state.syncMessage,
                    onSync = viewModel::syncSaves,
                )
            }
        }
    }
}

@Composable
private fun DownloadSection(
    download: DownloadedGame?,
    fileSize: Long,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Download", style = MaterialTheme.typography.titleMedium)

            when (download?.status) {
                null, DownloadStatus.FAILED -> {
                    Text(
                        if (download?.status == DownloadStatus.FAILED) "Download failed. Tap to retry." else "Not downloaded yet",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Button(onClick = onDownload, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Download (${formatFileSize(fileSize)})")
                    }
                }
                DownloadStatus.PENDING -> {
                    Text("Waiting to start...", style = MaterialTheme.typography.bodySmall)
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancel")
                    }
                }
                DownloadStatus.DOWNLOADING -> {
                    val progress = if (fileSize > 0) download.bytesDownloaded.toFloat() / fileSize else 0f
                    Text(
                        "${formatFileSize(download.bytesDownloaded)} / ${formatFileSize(fileSize)}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancel")
                    }
                }
                DownloadStatus.PAUSED -> {
                    Text("Download paused", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onDownload, modifier = Modifier.weight(1f)) { Text("Resume") }
                        OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f)) { Text("Remove") }
                    }
                }
                DownloadStatus.COMPLETED -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Downloaded", style = MaterialTheme.typography.bodyMedium)
                    }
                    OutlinedButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Delete from device")
                    }
                }
            }
        }
    }
}

@Composable
private fun SaveSyncSection(
    syncState: SaveSyncState?,
    isSyncing: Boolean,
    syncMessage: String?,
    onSync: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Save Files", style = MaterialTheme.typography.titleMedium)

            if (syncState == null) {
                Text("No save data yet. Play the game and saves will sync automatically.", style = MaterialTheme.typography.bodySmall)
            } else {
                val statusText = when (syncState.syncStatus) {
                    SyncStatus.IN_SYNC -> "Saves are in sync"
                    SyncStatus.LOCAL_NEWER -> "Local save is newer — will upload"
                    SyncStatus.SERVER_NEWER -> "Server save is newer — will download"
                    SyncStatus.NEVER_SYNCED -> "Never synced"
                    SyncStatus.SYNCING -> "Syncing..."
                }
                Text(statusText, style = MaterialTheme.typography.bodySmall)

                syncState.lastSyncAt?.let { ts ->
                    val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(ts))
                    Text("Last synced: $dateStr", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                syncState.localSaveFileName?.let { name ->
                    Text("Local: $name", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            syncMessage?.let { msg ->
                Text(
                    msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (msg.startsWith("Sync failed")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }

            OutlinedButton(onClick = onSync, enabled = !isSyncing, modifier = Modifier.fillMaxWidth()) {
                if (isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Sync, null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(if (isSyncing) "Syncing..." else "Sync Now")
            }
        }
    }
}
