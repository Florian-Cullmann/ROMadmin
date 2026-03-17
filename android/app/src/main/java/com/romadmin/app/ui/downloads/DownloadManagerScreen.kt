package com.romadmin.app.ui.downloads

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.romadmin.app.data.local.entity.DownloadStatus
import com.romadmin.app.data.local.entity.DownloadedGame
import com.romadmin.app.data.repository.DownloadRepository
import com.romadmin.app.util.formatFileSize
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadManagerViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository,
) : ViewModel() {

    val downloads = downloadRepository.getAllDownloads()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun cancelDownload(gameId: Int) {
        viewModelScope.launch { downloadRepository.cancelDownload(gameId) }
    }

    fun deleteDownload(gameId: Int) {
        viewModelScope.launch { downloadRepository.deleteDownload(gameId) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadManagerScreen(
    viewModel: DownloadManagerViewModel = hiltViewModel(),
) {
    val downloads by viewModel.downloads.collectAsState()

    val active = downloads.filter { it.status in listOf(DownloadStatus.PENDING, DownloadStatus.DOWNLOADING) }
    val completed = downloads.filter { it.status == DownloadStatus.COMPLETED }
    val failed = downloads.filter { it.status in listOf(DownloadStatus.FAILED, DownloadStatus.PAUSED) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Downloads") })
        }
    ) { padding ->
        if (downloads.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Text("No downloads yet", style = MaterialTheme.typography.bodyLarge)
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (active.isNotEmpty()) {
                item { Text("Active", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 4.dp)) }
                items(active, key = { it.gameId }) { dl ->
                    DownloadItem(dl, onCancel = { viewModel.cancelDownload(dl.gameId) }, onDelete = { viewModel.deleteDownload(dl.gameId) })
                }
            }

            if (failed.isNotEmpty()) {
                item { Text("Failed", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 4.dp)) }
                items(failed, key = { it.gameId }) { dl ->
                    DownloadItem(dl, onCancel = {}, onDelete = { viewModel.deleteDownload(dl.gameId) })
                }
            }

            if (completed.isNotEmpty()) {
                item { Text("Completed (${completed.size})", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 4.dp)) }
                items(completed, key = { it.gameId }) { dl ->
                    DownloadItem(dl, onCancel = {}, onDelete = { viewModel.deleteDownload(dl.gameId) })
                }
            }
        }
    }
}

@Composable
private fun DownloadItem(
    download: DownloadedGame,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    download.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                when (download.status) {
                    DownloadStatus.DOWNLOADING -> {
                        val progress = if (download.fileSize > 0) download.bytesDownloaded.toFloat() / download.fileSize else 0f
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        )
                        Text(
                            "${formatFileSize(download.bytesDownloaded)} / ${formatFileSize(download.fileSize)}",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    DownloadStatus.PENDING -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                        Text("Waiting...", style = MaterialTheme.typography.labelSmall)
                    }
                    DownloadStatus.COMPLETED -> {
                        Text(formatFileSize(download.fileSize), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DownloadStatus.FAILED -> {
                        Text("Failed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                    DownloadStatus.PAUSED -> {
                        Text("Paused", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            when (download.status) {
                DownloadStatus.DOWNLOADING, DownloadStatus.PENDING -> {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, "Cancel")
                    }
                }
                else -> {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            }
        }
    }
}
