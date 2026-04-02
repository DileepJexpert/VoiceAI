package com.voiceai.app.presentation.search

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.voiceai.app.presentation.navigation.Routes
import com.voiceai.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = uiState.query,
                        onQueryChange = viewModel::updateQuery,
                        onSearch = {},
                        expanded = false,
                        onExpandedChange = {},
                        modifier = Modifier.focusRequester(focusRequester),
                        placeholder = { Text("Search notes and scans") },
                        leadingIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        trailingIcon = {
                            if (uiState.query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearSearch() }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Clear"
                                    )
                                }
                            }
                        }
                    )
                },
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {}

            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedFilter == SearchFilter.ALL,
                    onClick = { viewModel.setFilter(SearchFilter.ALL) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = uiState.selectedFilter == SearchFilter.VOICE_NOTES,
                    onClick = { viewModel.setFilter(SearchFilter.VOICE_NOTES) },
                    label = { Text("Voice Notes") }
                )
                FilterChip(
                    selected = uiState.selectedFilter == SearchFilter.SCANNED_DOCS,
                    onClick = { viewModel.setFilter(SearchFilter.SCANNED_DOCS) },
                    label = { Text("Scanned Docs") }
                )
            }

            // Content area
            when {
                uiState.isSearching -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.query.isEmpty() -> {
                    InitialState()
                }

                else -> {
                    val showVoiceNotes = uiState.selectedFilter != SearchFilter.SCANNED_DOCS
                    val showScans = uiState.selectedFilter != SearchFilter.VOICE_NOTES

                    val voiceNotes = if (showVoiceNotes) uiState.voiceNoteResults else emptyList()
                    val scans = if (showScans) uiState.scanResults else emptyList()

                    if (voiceNotes.isEmpty() && scans.isEmpty()) {
                        EmptyResultsState()
                    } else {
                        SearchResultsList(
                            voiceNotes = voiceNotes,
                            scans = scans,
                            query = uiState.query,
                            onVoiceNoteClick = { noteId ->
                                navController.navigate(Routes.noteDetail(noteId))
                            },
                            onScanClick = { scanId ->
                                navController.navigate(Routes.scanDetail(scanId))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultsList(
    voiceNotes: List<com.voiceai.app.domain.model.VoiceNote>,
    scans: List<com.voiceai.app.domain.model.ScannedDocument>,
    query: String,
    onVoiceNoteClick: (Long) -> Unit,
    onScanClick: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = voiceNotes,
            key = { "note_${it.id}" }
        ) { note ->
            ListItem(
                headlineContent = {
                    Text(
                        text = note.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Column {
                        HighlightedText(
                            text = note.transcript ?: note.summary ?: "",
                            query = query,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = DateUtils.formatRelativeTime(note.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Voice note",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.clickable { onVoiceNoteClick(note.id) }
            )
        }

        items(
            items = scans,
            key = { "scan_${it.id}" }
        ) { doc ->
            ListItem(
                headlineContent = {
                    Text(
                        text = doc.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Column {
                        HighlightedText(
                            text = doc.extractedText ?: doc.summary ?: "",
                            query = query,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = DateUtils.formatRelativeTime(doc.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = "Scanned document",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.clickable { onScanClick(doc.id) }
            )
        }
    }
}

@Composable
private fun HighlightedText(
    text: String,
    query: String,
    maxLines: Int
) {
    if (text.isBlank() || query.isBlank()) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val highlightColor = MaterialTheme.colorScheme.primary
    val annotatedString = buildAnnotatedString {
        var startIndex = 0
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()

        while (startIndex < text.length) {
            val matchIndex = lowerText.indexOf(lowerQuery, startIndex)
            if (matchIndex == -1) {
                append(text.substring(startIndex))
                break
            }
            append(text.substring(startIndex, matchIndex))
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = highlightColor)) {
                append(text.substring(matchIndex, matchIndex + query.length))
            }
            startIndex = matchIndex + query.length
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodySmall,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun InitialState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Search your notes and scans",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyResultsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No results found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Try a different search term",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
