package com.romadmin.app.ui.platforms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.romadmin.app.data.repository.PlatformRepository
import com.romadmin.app.domain.model.Platform
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlatformListState(
    val platforms: List<Platform> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class PlatformListViewModel @Inject constructor(
    private val platformRepository: PlatformRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PlatformListState())
    val state = _state.asStateFlow()

    init { loadPlatforms() }

    fun loadPlatforms() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val platforms = platformRepository.getPlatforms()
                _state.value = PlatformListState(
                    platforms = platforms.sortedBy { it.sortOrder },
                    isLoading = false,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformListScreen(
    onPlatformClick: (Int) -> Unit,
    viewModel: PlatformListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("RomAdmin") })
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Failed to load", style = MaterialTheme.typography.bodyLarge)
                        Text(state.error ?: "", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = viewModel::loadPlatforms) { Text("Retry") }
                    }
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize().padding(padding),
                ) {
                    items(state.platforms, key = { it.id }) { platform ->
                        PlatformCard(platform = platform, onClick = { onPlatformClick(platform.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun PlatformCard(platform: Platform, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (platform.thumbnailUrl != null) {
                AsyncImage(
                    model = platform.thumbnailUrl,
                    contentDescription = platform.displayName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(64.dp),
                )
                Spacer(Modifier.height(8.dp))
            }

            Text(
                text = platform.displayName,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
            )

            platform._count?.let { count ->
                Text(
                    text = "${count.games} games",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
