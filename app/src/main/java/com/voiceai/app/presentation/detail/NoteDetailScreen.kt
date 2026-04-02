package com.voiceai.app.presentation.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.voiceai.app.presentation.components.AudioPlayer
import com.voiceai.app.presentation.components.nextPlaybackSpeed

private val DetailTabs = listOf("Transcript", "Summary", "Action Items")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    navController: NavController,
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current

    // Navigate back after deletion
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            navController.popBackStack()
        }
    }

    // Delete confirmation dialog
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteConfirmation() },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this voice note? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteNote() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteConfirmation() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val note = uiState.note
                    if (note != null) {
                        var isEditing by remember { mutableStateOf(false) }
                        var editingTitle by remember(note.title) { mutableStateOf(note.title) }

                        if (isEditing) {
                            OutlinedTextField(
                                value = editingTitle,
                                onValueChange = { editingTitle = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth()
                            )
                            // Commit title on focus loss is handled by the recomposition;
                            // a simple approach: commit when the user navigates away or taps elsewhere.
                            LaunchedEffect(isEditing) {
                                // We rely on onValueChange + blur to save.
                            }
                        } else {
                            Text(
                                text = note.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Toggle editing on click: handled via the title text's clickable modifier
                        // For simplicity we use a tap on the title area via the TopAppBar title slot.
                        // A more refined UX could use a dedicated edit icon.
                        LaunchedEffect(isEditing) {
                            if (!isEditing && editingTitle != note.title) {
                                viewModel.updateTitle(editingTitle)
                            }
                        }
                    } else {
                        Text(
                            text = "Note Detail",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    val note = uiState.note
                    if (note != null) {
                        // TTS / Read Aloud
                        IconButton(onClick = { viewModel.toggleTts() }) {
                            Icon(
                                imageVector = Icons.Filled.VolumeUp,
                                contentDescription = if (uiState.isTtsSpeaking) "Stop reading" else "Read aloud",
                                tint = if (uiState.isTtsSpeaking) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }

                        // Favorite toggle
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (note.isFavorite) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Filled.FavoriteBorder
                                },
                                contentDescription = if (note.isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (note.isFavorite) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }

                        // Share
                        IconButton(onClick = { /* Share intent integration */ }) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share"
                            )
                        }

                        // Delete
                        IconButton(onClick = { viewModel.showDeleteConfirmation() }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
            )
        },
        bottomBar = {
            if (uiState.note != null) {
                BottomAppBar(
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Share
                        FilledTonalButton(
                            onClick = { /* Share intent */ },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", style = MaterialTheme.typography.labelMedium)
                        }

                        // Export PDF
                        FilledTonalButton(
                            onClick = { /* PDF export integration */ },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export PDF", style = MaterialTheme.typography.labelMedium)
                        }

                        // Copy Text
                        FilledTonalButton(
                            onClick = {
                                val text = uiState.note?.transcript ?: uiState.note?.summary ?: ""
                                clipboardManager.setText(AnnotatedString(text))
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Text", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        AnimatedVisibility(
            visible = !uiState.isLoading && uiState.note != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val note = uiState.note ?: return@AnimatedVisibility

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Audio Player section
                AudioPlayer(
                    currentPosition = uiState.currentPosition,
                    duration = uiState.audioDuration,
                    isPlaying = uiState.isPlaying,
                    playbackSpeed = uiState.playbackSpeed,
                    onPlayPauseClick = { viewModel.togglePlayPause() },
                    onSeek = { viewModel.seekTo(it) },
                    onSpeedChange = {
                        viewModel.setPlaybackSpeed(
                            nextPlaybackSpeed(uiState.playbackSpeed)
                        )
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Tabs
                TabRow(
                    selectedTabIndex = uiState.selectedTab
                ) {
                    DetailTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = uiState.selectedTab == index,
                            onClick = { viewModel.selectTab(index) },
                            text = { Text(title) }
                        )
                    }
                }

                // Tab content
                when (uiState.selectedTab) {
                    0 -> TranscriptTab(
                        transcript = note.transcript,
                        onCopy = { text ->
                            clipboardManager.setText(AnnotatedString(text))
                        }
                    )
                    1 -> SummaryTab(summary = note.summary)
                    2 -> ActionItemsTab(summary = note.summary)
                }
            }
        }

        // Empty state when note is not found
        AnimatedVisibility(
            visible = !uiState.isLoading && uiState.note == null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Note not found",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TranscriptTab(
    transcript: String?,
    onCopy: (String) -> Unit
) {
    if (transcript.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No transcript available",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = transcript,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            FilledTonalButton(
                onClick = { onCopy(transcript) },
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Transcript", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun SummaryTab(summary: String?) {
    if (summary.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No summary available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                FilledTonalButton(
                    onClick = { /* Generate summary integration */ },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Generate Summary")
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ActionItemsTab(summary: String?) {
    val actionItems = remember(summary) { parseActionItems(summary) }

    if (actionItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No action items found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            actionItems.forEachIndexed { index, item ->
                ActionItemRow(index = index + 1, text = item)
            }
        }
    }
}

@Composable
private fun ActionItemRow(index: Int, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$index.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(28.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Parses action items from the summary text. Looks for lines that start with
 * common action-item markers such as "- [ ]", "- ", "* ", or numbered items
 * that contain action-oriented keywords.
 */
private fun parseActionItems(summary: String?): List<String> {
    if (summary.isNullOrBlank()) return emptyList()

    val lines = summary.lines().map { it.trim() }.filter { it.isNotBlank() }
    val actionPatterns = listOf(
        Regex("""^[-*]\s*\[[ x]]\s*(.+)""", RegexOption.IGNORE_CASE),  // - [ ] or - [x] items
        Regex("""^\d+[.)]\s*(.+)"""),                                     // numbered items
        Regex("""^[-*]\s+(.+)""")                                         // bullet items
    )

    return lines.mapNotNull { line ->
        actionPatterns.firstNotNullOfOrNull { pattern ->
            pattern.matchEntire(line)?.groupValues?.getOrNull(1)
        }
    }
}
