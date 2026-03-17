package com.romadmin.app.ui.games

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.romadmin.app.data.local.dao.DownloadDao
import com.romadmin.app.data.local.entity.DownloadStatus
import com.romadmin.app.data.local.entity.DownloadedGame
import com.romadmin.app.data.repository.DownloadRepository
import com.romadmin.app.data.repository.GameRepository
import com.romadmin.app.data.repository.PlatformRepository
import com.romadmin.app.domain.model.Game
import com.romadmin.app.domain.model.Platform
import com.romadmin.app.util.formatFileSizeFromString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GameListState(
    val platform: Platform? = null,
    val games: List<Game> = emptyList(),
    val downloads: Map<Int, DownloadedGame> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val search: String = "",
    val page: Int = 1,
    val totalPages: Int = 1,
    val isDownloadingAll: Boolean = false,
)

@HiltViewModel
class GameListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gameRepository: GameRepository,
    private val platformRepository: PlatformRepository,
    private val downloadRepository: DownloadRepository,
    private val downloadDao: DownloadDao,
) : ViewModel() {

    private val platformId: Int = savedStateHandle["platformId"] ?: 0
    private val _state = MutableStateFlow(GameListState())
    val state = _state.asStateFlow()

    init {
        loadPlatform()
        loadGames()
        observeDownloads()
    }

    private fun loadPlatform() {
        viewModelScope.launch {
            try {
                val platform = platformRepository.getPlatform(platformId)
                _state.value = _state.value.copy(platform = platform)
            } catch (_: Exception) {}
        }
    }

    fun loadGames(page: Int = 1) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val search = _state.value.search.ifBlank { null }
                val response = gameRepository.getGames(
                    platformId = platformId,
                    search = search,
                    sort = "displayName",
                    page = page,
                    limit = 48,
                )
                _state.value = _state.value.copy(
                    games = response.data,
                    isLoading = false,
                    page = response.page,
                    totalPages = response.totalPages,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun observeDownloads() {
        viewModelScope.launch {
            downloadDao.getByPlatformId(platformId).collect { downloads ->
                _state.value = _state.value.copy(
                    downloads = downloads.associateBy { it.gameId }
                )
            }
        }
    }

    fun updateSearch(search: String) {
        _state.value = _state.value.copy(search = search)
        loadGames()
    }

    fun downloadAll() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isDownloadingAll = true)
            try {
                val platform = _state.value.platform ?: return@launch
                val manifest = gameRepository.getPlatformManifest(platformId)
                downloadRepository.enqueuePlatformDownload(
                    platformId = platformId,
                    platformFolderName = platform.folderName,
                    games = manifest.games,
                )
            } catch (_: Exception) {}
            _state.value = _state.value.copy(isDownloadingAll = false)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameListScreen(
    onGameClick: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: GameListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.platform?.displayName ?: "Games") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::downloadAll,
                        enabled = !state.isDownloadingAll,
                    ) {
                        Icon(Icons.Default.Download, "Download All")
                    }
                },
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search bar
            OutlinedTextField(
                value = state.search,
                onValueChange = viewModel::updateSearch,
                placeholder = { Text("Search games...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error: ${state.error}")
                            Button(onClick = { viewModel.loadGames() }) { Text("Retry") }
                        }
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 120.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.games, key = { it.id }) { game ->
                            GameCard(
                                game = game,
                                download = state.downloads[game.id],
                                onClick = { onGameClick(game.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameCard(game: Game, download: DownloadedGame?, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Box {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f),
                ) {
                    AsyncImage(
                        model = game.coverUrl,
                        contentDescription = game.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )

                    // Download status badge
                    when (download?.status) {
                        DownloadStatus.COMPLETED -> {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Downloaded",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(20.dp),
                            )
                        }
                        DownloadStatus.DOWNLOADING -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        }
                        DownloadStatus.PENDING -> {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = "Pending",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(20.dp),
                            )
                        }
                        else -> {}
                    }
                }

                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = game.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = formatFileSizeFromString(game.fileSize),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
