package com.voiceai.app.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.voiceai.app.presentation.components.NoteCard
import com.voiceai.app.presentation.components.ScanCard
import com.voiceai.app.presentation.navigation.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "VoiceAI",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.SEARCH) }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = uiState.selectedTab.ordinal
            ) {
                Tab(
                    selected = uiState.selectedTab == HomeTab.VOICE_NOTES,
                    onClick = { viewModel.selectTab(HomeTab.VOICE_NOTES) },
                    text = { Text("Voice Notes") }
                )
                Tab(
                    selected = uiState.selectedTab == HomeTab.SCANNED_DOCS,
                    onClick = { viewModel.selectTab(HomeTab.SCANNED_DOCS) },
                    text = { Text("Scanned Docs") }
                )
            }

            var isRefreshing by remember { mutableStateOf(false) }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    // Data is flow-based so it refreshes automatically;
                    // just toggle the indicator briefly.
                    isRefreshing = false
                },
                modifier = Modifier.fillMaxSize()
            ) {
                AnimatedVisibility(
                    visible = uiState.isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                AnimatedVisibility(
                    visible = !uiState.isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    when (uiState.selectedTab) {
                        HomeTab.VOICE_NOTES -> {
                            if (uiState.voiceNotes.isEmpty()) {
                                EmptyState(
                                    icon = Icons.Filled.Mic,
                                    title = "No voice notes yet",
                                    subtitle = "Tap the + button to record your first note"
                                )
                            } else {
                                VoiceNotesList(
                                    uiState = uiState,
                                    onNoteClick = { noteId ->
                                        navController.navigate(Routes.noteDetail(noteId))
                                    },
                                    onDelete = { noteId ->
                                        viewModel.deleteVoiceNote(noteId)
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Voice note deleted",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                // Undo would require caching the deleted item;
                                                // repository-level undo can be added later.
                                            }
                                        }
                                    },
                                    onFavoriteToggle = { noteId ->
                                        viewModel.toggleVoiceNoteFavorite(noteId)
                                    }
                                )
                            }
                        }

                        HomeTab.SCANNED_DOCS -> {
                            if (uiState.scannedDocuments.isEmpty()) {
                                EmptyState(
                                    icon = Icons.Filled.Description,
                                    title = "No scanned documents yet",
                                    subtitle = "Tap the + button to scan your first document"
                                )
                            } else {
                                ScannedDocsList(
                                    uiState = uiState,
                                    onDocClick = { scanId ->
                                        navController.navigate(Routes.scanDetail(scanId))
                                    },
                                    onDelete = { scanId ->
                                        viewModel.deleteScannedDocument(scanId)
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Document deleted",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                // Undo placeholder
                                            }
                                        }
                                    },
                                    onFavoriteToggle = { scanId ->
                                        viewModel.toggleScannedDocFavorite(scanId)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceNotesList(
    uiState: HomeUiState,
    onNoteClick: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onFavoriteToggle: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = uiState.voiceNotes,
            key = { it.id }
        ) { note ->
            NoteCard(
                note = note,
                onClick = { onNoteClick(note.id) },
                onDelete = { onDelete(note.id) },
                onFavoriteToggle = { onFavoriteToggle(note.id) }
            )
        }
    }
}

@Composable
private fun ScannedDocsList(
    uiState: HomeUiState,
    onDocClick: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onFavoriteToggle: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = uiState.scannedDocuments,
            key = { it.id }
        ) { document ->
            ScanCard(
                document = document,
                onClick = { onDocClick(document.id) },
                onDelete = { onDelete(document.id) },
                onFavoriteToggle = { onFavoriteToggle(document.id) }
            )
        }
    }
}

@Composable
private fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
