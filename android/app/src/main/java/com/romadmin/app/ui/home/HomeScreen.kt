package com.romadmin.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.romadmin.app.data.local.dao.DownloadDao
import com.romadmin.app.data.local.dao.SaveSyncDao
import com.romadmin.app.data.local.entity.DownloadStatus
import com.romadmin.app.data.local.entity.DownloadedGame
import com.romadmin.app.data.local.entity.SaveSyncState
import com.romadmin.app.data.local.entity.SyncStatus
import com.romadmin.app.data.repository.SaveSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class GameSyncItem(
    val download: DownloadedGame,
    val syncState: SaveSyncState?,
)

data class HomeState(
    val games: List<GameSyncItem> = emptyList(),
    val isSyncing: Boolean = false,
    val syncResult: SyncResult? = null,
)

data class SyncResult(
    val uploaded: Int,
    val downloaded: Int,
    val inSync: Int,
    val failed: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val downloadDao: DownloadDao,
    private val saveSyncDao: SaveSyncDao,
    private val saveSyncRepository: SaveSyncRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                downloadDao.getByStatus(DownloadStatus.COMPLETED),
                saveSyncDao.getAll(),
            ) { downloads, syncStates ->
                val syncMap = syncStates.associateBy { it.gameId }
                downloads.map { dl ->
                    GameSyncItem(download = dl, syncState = syncMap[dl.gameId])
                }
            }.collect { games ->
                _state.value = _state.value.copy(games = games)
            }
        }
    }

    fun syncAll() {
        _state.value = _state.value.copy(isSyncing = true, syncResult = null)
        viewModelScope.launch {
            try {
                val count = withContext(Dispatchers.IO) { saveSyncRepository.performSync() }
                // Read updated sync states to count results
                val syncStates = saveSyncDao.getAll().first()
                val inSyncCount = syncStates.count { it.syncStatus == SyncStatus.IN_SYNC }
                _state.value = _state.value.copy(
                    isSyncing = false,
                    syncResult = SyncResult(
                        uploaded = count, // performSync returns files that were uploaded or downloaded
                        downloaded = 0,
                        inSync = inSyncCount,
                    ),
                )
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Sync failed", e)
                _state.value = _state.value.copy(
                    isSyncing = false,
                    syncResult = SyncResult(
                        uploaded = 0, downloaded = 0, inSync = 0,
                        failed = true,
                        errorMessage = e.message ?: "${e.javaClass.simpleName}: ${e.cause?.message ?: "no details"}",
                    ),
                )
            }
        }
    }

    fun dismissResult() {
        _state.value = _state.value.copy(syncResult = null)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("RomAdmin") })
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            // Sync All button
            item {
                SyncAllCard(
                    gameCount = state.games.size,
                    isSyncing = state.isSyncing,
                    syncResult = state.syncResult,
                    onSync = viewModel::syncAll,
                    onDismissResult = viewModel::dismissResult,
                )
            }

            // Game list header
            if (state.games.isNotEmpty()) {
                item {
                    Text(
                        "Downloaded Games",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                items(state.games, key = { it.download.gameId }) { item ->
                    GameSyncCard(item)
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                Icons.Default.SportsEsports,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "No games downloaded yet",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                "Download games from the Library tab to start syncing saves",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncAllCard(
    gameCount: Int,
    isSyncing: Boolean,
    syncResult: SyncResult?,
    onSync: () -> Unit,
    onDismissResult: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Sync,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Save Sync",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        if (gameCount > 0) "$gameCount downloaded game${if (gameCount != 1) "s" else ""}"
                        else "No games to sync",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    )
                }
            }

            Button(
                onClick = onSync,
                enabled = !isSyncing && gameCount > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Syncing...", style = MaterialTheme.typography.titleMedium)
                } else {
                    Icon(Icons.Default.CloudSync, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Sync All Games", style = MaterialTheme.typography.titleMedium)
                }
            }

            // Sync result banner
            if (syncResult != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (syncResult.failed)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.tertiaryContainer,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (syncResult.failed) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Sync failed: ${syncResult.errorMessage ?: "Unknown error"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f),
                            )
                        } else {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${syncResult.uploaded} synced, ${syncResult.inSync} already up to date",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        IconButton(onClick = onDismissResult, modifier = Modifier.size(24.dp)) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss",
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameSyncCard(item: GameSyncItem) {
    val dl = item.download
    val sync = item.syncState
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Sync status icon
            SyncStatusIcon(sync)

            Spacer(Modifier.width(12.dp))

            // Game info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    dl.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )

                // Sync status text
                val (statusText, statusColor) = syncStatusInfo(sync)
                Text(
                    statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor,
                )

                // Last sync time
                sync?.lastSyncAt?.let { ts ->
                    Text(
                        "Last sync: ${dateFormat.format(Date(ts))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Save file indicator
            if (sync?.localSaveFileName != null) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            sync.localSaveFileName.substringAfterLast('.'),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    modifier = Modifier.height(24.dp),
                )
            }
        }
    }
}

@Composable
private fun SyncStatusIcon(sync: SaveSyncState?) {
    val (icon, tint, bgColor) = when (sync?.syncStatus) {
        SyncStatus.IN_SYNC -> Triple(
            Icons.Default.CloudDone,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer,
        )
        SyncStatus.LOCAL_NEWER -> Triple(
            Icons.Default.CloudUpload,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.tertiaryContainer,
        )
        SyncStatus.SERVER_NEWER -> Triple(
            Icons.Default.CloudDownload,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.secondaryContainer,
        )
        SyncStatus.SYNCING -> Triple(
            Icons.Default.Sync,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer,
        )
        SyncStatus.NEVER_SYNCED, null -> Triple(
            Icons.Default.CloudOff,
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.surfaceVariant,
        )
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = bgColor,
        modifier = Modifier.size(40.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun syncStatusInfo(sync: SaveSyncState?): Pair<String, androidx.compose.ui.graphics.Color> {
    return when (sync?.syncStatus) {
        SyncStatus.IN_SYNC -> "In sync" to MaterialTheme.colorScheme.primary
        SyncStatus.LOCAL_NEWER -> "Local save is newer" to MaterialTheme.colorScheme.tertiary
        SyncStatus.SERVER_NEWER -> "Server save is newer" to MaterialTheme.colorScheme.secondary
        SyncStatus.SYNCING -> "Syncing..." to MaterialTheme.colorScheme.primary
        SyncStatus.NEVER_SYNCED -> "Never synced" to MaterialTheme.colorScheme.onSurfaceVariant
        null -> "No save data" to MaterialTheme.colorScheme.onSurfaceVariant
    }
}
