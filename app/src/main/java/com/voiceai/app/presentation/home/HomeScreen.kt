package com.voiceai.app.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Tab
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.voiceai.app.domain.model.ActionItem
import com.voiceai.app.presentation.components.NoteCard
import com.voiceai.app.presentation.components.ScanCard
import com.voiceai.app.presentation.navigation.Routes
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                edgePadding = 16.dp
            ) {
                Tab(
                    selected = uiState.selectedTab == HomeTab.ALL,
                    onClick = { viewModel.selectTab(HomeTab.ALL) },
                    text = { Text("All") }
                )
                Tab(
                    selected = uiState.selectedTab == HomeTab.VOICE_NOTES,
                    onClick = { viewModel.selectTab(HomeTab.VOICE_NOTES) },
                    text = { Text("Voice Notes") }
                )
                Tab(
                    selected = uiState.selectedTab == HomeTab.SCANNED_DOCS,
                    onClick = { viewModel.selectTab(HomeTab.SCANNED_DOCS) },
                    text = { Text("Scans") }
                )
                Tab(
                    selected = uiState.selectedTab == HomeTab.ACTION_ITEMS,
                    onClick = { viewModel.selectTab(HomeTab.ACTION_ITEMS) },
                    text = { Text("Action Items") }
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
                        HomeTab.ALL -> {
                            val allEmpty = uiState.voiceNotes.isEmpty() && uiState.scannedDocuments.isEmpty()
                            if (allEmpty) {
                                EmptyState(
                                    icon = Icons.Filled.Mic,
                                    title = "Nothing here yet",
                                    subtitle = "Tap the + button to record a note or scan a document"
                                )
                            } else {
                                UnifiedFeedList(
                                    uiState = uiState,
                                    onNoteClick = { noteId ->
                                        navController.navigate(Routes.noteDetail(noteId))
                                    },
                                    onScanClick = { scanId ->
                                        navController.navigate(Routes.scanDetail(scanId))
                                    },
                                    onDeleteNote = { noteId ->
                                        viewModel.deleteVoiceNote(noteId)
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Voice note deleted",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    },
                                    onDeleteScan = { scanId ->
                                        viewModel.deleteScannedDocument(scanId)
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Document deleted",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    },
                                    onFavoriteToggleNote = { noteId ->
                                        viewModel.toggleVoiceNoteFavorite(noteId)
                                    },
                                    onFavoriteToggleScan = { scanId ->
                                        viewModel.toggleScannedDocFavorite(scanId)
                                    }
                                )
                            }
                        }

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

                        HomeTab.ACTION_ITEMS -> {
                            if (uiState.actionItems.isEmpty()) {
                                EmptyState(
                                    icon = Icons.Filled.CheckCircle,
                                    title = "No action items",
                                    subtitle = "Action items from your notes will appear here"
                                )
                            } else {
                                ActionItemsList(
                                    actionItems = uiState.actionItems,
                                    onStatusToggle = { id, currentStatus ->
                                        val newStatus = if (currentStatus == "pending") "completed" else "pending"
                                        viewModel.updateActionItemStatus(id, newStatus)
                                    },
                                    onDelete = { id ->
                                        viewModel.deleteActionItem(id)
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Action item deleted",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
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

/**
 * Sealed class representing items in the unified "All" feed,
 * allowing voice notes and scans to be interleaved by date.
 */
private sealed class FeedItem(val createdAt: Long) {
    class Note(val note: com.voiceai.app.domain.model.VoiceNote) : FeedItem(note.createdAt)
    class Scan(val doc: com.voiceai.app.domain.model.ScannedDocument) : FeedItem(doc.createdAt)
}

@Composable
private fun UnifiedFeedList(
    uiState: HomeUiState,
    onNoteClick: (Long) -> Unit,
    onScanClick: (Long) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onDeleteScan: (Long) -> Unit,
    onFavoriteToggleNote: (Long) -> Unit,
    onFavoriteToggleScan: (Long) -> Unit
) {
    val feedItems = remember(uiState.voiceNotes, uiState.scannedDocuments) {
        val notes = uiState.voiceNotes.map { FeedItem.Note(it) }
        val scans = uiState.scannedDocuments.map { FeedItem.Scan(it) }
        (notes + scans).sortedByDescending { it.createdAt }
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = feedItems,
            key = { item ->
                when (item) {
                    is FeedItem.Note -> "note_${item.note.id}"
                    is FeedItem.Scan -> "scan_${item.doc.id}"
                }
            }
        ) { item ->
            when (item) {
                is FeedItem.Note -> NoteCard(
                    note = item.note,
                    onClick = { onNoteClick(item.note.id) },
                    onDelete = { onDeleteNote(item.note.id) },
                    onFavoriteToggle = { onFavoriteToggleNote(item.note.id) }
                )
                is FeedItem.Scan -> ScanCard(
                    document = item.doc,
                    onClick = { onScanClick(item.doc.id) },
                    onDelete = { onDeleteScan(item.doc.id) },
                    onFavoriteToggle = { onFavoriteToggleScan(item.doc.id) }
                )
            }
        }
    }
}

@Composable
private fun ActionItemsList(
    actionItems: List<ActionItem>,
    onStatusToggle: (Long, String) -> Unit,
    onDelete: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = actionItems,
            key = { it.id }
        ) { actionItem ->
            ActionItemCard(
                actionItem = actionItem,
                onStatusToggle = { onStatusToggle(actionItem.id, actionItem.status) },
                onDelete = { onDelete(actionItem.id) }
            )
        }
    }
}

@Composable
private fun ActionItemCard(
    actionItem: ActionItem,
    onStatusToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = when (actionItem.priority) {
        1 -> Color(0xFFE74C3C) // Red - high
        2 -> Color(0xFFF39C12) // Yellow/amber - medium
        else -> Color(0xFF2ECC71) // Green - low
    }

    val dateFormatter = remember {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Priority color strip on left edge
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(80.dp)
                    .background(priorityColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Priority dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(priorityColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Title
                    Text(
                        text = actionItem.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status chip
                    SuggestionChip(
                        onClick = onStatusToggle,
                        label = {
                            Text(
                                text = actionItem.status.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )

                    // Due date
                    if (actionItem.dueDate != null) {
                        Text(
                            text = "Due ${dateFormatter.format(Date(actionItem.dueDate))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Source link text
                val sourceText = when {
                    actionItem.sourceNoteId != null -> "From voice note"
                    actionItem.sourceScanId != null -> "From scanned document"
                    else -> null
                }
                if (sourceText != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sourceText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
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
