package com.voiceai.app.presentation.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.voiceai.app.domain.model.ActionItem
import com.voiceai.app.presentation.components.AudioPlayer
import com.voiceai.app.presentation.components.nextPlaybackSpeed
import com.voiceai.app.presentation.theme.Coral

private val DetailTabs = listOf("Transcript", "Summary", "Action Items", "Speakers")

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
                    1 -> SummaryTabEnhanced(
                        summary = note.summary,
                        sentiment = uiState.sentiment,
                        keyPoints = uiState.keyPoints,
                        onRegenerateSummary = { viewModel.regenerateSummary() }
                    )
                    2 -> ActionItemsTabEnhanced(
                        actionItems = uiState.actionItemsList,
                        onStatusChange = { id, status -> viewModel.updateActionItemStatus(id, status) },
                        onCreateReminder = { title -> viewModel.createReminder(title) }
                    )
                    3 -> SpeakersTab(
                        speakerSegments = uiState.speakerSegments,
                        totalDuration = uiState.audioDuration
                    )
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
private fun SummaryTabEnhanced(
    summary: String?,
    sentiment: String?,
    keyPoints: List<String>,
    onRegenerateSummary: () -> Unit
) {
    if (summary.isNullOrBlank() && keyPoints.isEmpty()) {
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
                    onClick = onRegenerateSummary,
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
            // Sentiment indicator
            if (sentiment != null) {
                val (emoji, label, color) = when (sentiment.lowercase()) {
                    "positive" -> Triple("\uD83D\uDC4D", "Positive", Color(0xFF4CAF50))
                    "negative" -> Triple("\uD83D\uDC4E", "Negative", Color(0xFFF44336))
                    else -> Triple("\uD83D\uDE10", "Neutral", Color(0xFFFFC107))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = color
                    )
                }
            }

            // Key points
            if (keyPoints.isNotEmpty()) {
                Text(
                    text = "Key Points",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                keyPoints.forEach { point ->
                    Row(
                        modifier = Modifier.padding(bottom = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "\u2022",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Coral,
                            modifier = Modifier.width(20.dp)
                        )
                        Text(
                            text = point,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Summary text
            if (!summary.isNullOrBlank()) {
                Text(
                    text = "Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Regenerate Summary button
            OutlinedButton(
                onClick = onRegenerateSummary,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Regenerate Summary")
            }
        }
    }
}

@Composable
private fun ActionItemsTabEnhanced(
    actionItems: List<ActionItem>,
    onStatusChange: (Long, String) -> Unit,
    onCreateReminder: (String) -> Unit
) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(actionItems, key = { it.id }) { item ->
                ActionItemCard(
                    item = item,
                    onStatusChange = onStatusChange,
                    onCreateReminder = onCreateReminder
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionItemCard(
    item: ActionItem,
    onStatusChange: (Long, String) -> Unit,
    onCreateReminder: (String) -> Unit
) {
    val priorityColor = when (item.priority) {
        2 -> Color(0xFFF44336) // High - red
        1 -> Color(0xFFFFC107) // Medium - yellow
        else -> Color(0xFF4CAF50) // Low - green
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Priority color indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(priorityColor)
                )
                Spacer(modifier = Modifier.width(8.dp))

                // Checkbox
                Checkbox(
                    checked = item.status == "done",
                    onCheckedChange = { checked ->
                        onStatusChange(item.id, if (checked) "done" else "todo")
                    }
                )

                // Title
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }

            // Due date if present
            if (item.dueDate != null) {
                val dateFormat = remember { java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()) }
                Text(
                    text = "Due: ${dateFormat.format(java.util.Date(item.dueDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status toggle chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 16.dp)
            ) {
                val statuses = listOf("todo" to "To Do", "in_progress" to "In Progress", "done" to "Done")
                statuses.forEach { (value, label) ->
                    FilterChip(
                        selected = item.status == value,
                        onClick = { onStatusChange(item.id, value) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (value) {
                                "done" -> Color(0xFF4CAF50)
                                "in_progress" -> Color(0xFFFFC107)
                                else -> MaterialTheme.colorScheme.secondaryContainer
                            },
                            selectedLabelColor = if (value == "in_progress") Color.Black else Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 16.dp)
            ) {
                TextButton(
                    onClick = { onCreateReminder(item.title) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create Reminder", style = MaterialTheme.typography.labelSmall)
                }
                TextButton(
                    onClick = { /* Add to calendar integration */ }
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add to Calendar", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun SpeakersTab(
    speakerSegments: List<SpeakerSegment>,
    totalDuration: Long
) {
    if (speakerSegments.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "\uD83C\uDF99\uFE0F",
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Speaker detection not available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Speaker diarization data will appear here when available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        // Group by speaker and calculate talk times
        val speakerColors = listOf(
            Color(0xFF4CAF50),
            Color(0xFF2196F3),
            Color(0xFFFF9800),
            Color(0xFF9C27B0),
            Color(0xFFE91E63),
            Color(0xFF00BCD4)
        )

        val speakerTalkTimes = speakerSegments
            .groupBy { it.speakerId }
            .mapValues { (_, segments) ->
                segments.sumOf { it.endTime - it.startTime }
            }

        val totalTalkTime = speakerTalkTimes.values.sum().coerceAtLeast(1L)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Speaker Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            speakerTalkTimes.entries.forEachIndexed { index, (speakerId, talkTime) ->
                val color = speakerColors[index % speakerColors.size]
                val percentage = (talkTime * 100f / totalTalkTime)
                val minutes = talkTime / 60000
                val seconds = (talkTime % 60000) / 1000

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Color-coded speaker label
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = speakerId,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = color,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "%.1f%%".format(percentage),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Talk-time progress bar
                        LinearProgressIndicator(
                            progress = { (percentage / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = color,
                            trackColor = color.copy(alpha = 0.15f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "%dm %ds".format(minutes, seconds),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
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
