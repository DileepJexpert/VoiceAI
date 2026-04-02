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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SmallFloatingActionButton
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
import com.voiceai.app.domain.model.ActionItem
import com.voiceai.app.domain.model.Expense
import com.voiceai.app.domain.model.ScannedContact
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
            // Search bar with voice search and Ask AI buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
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
                            placeholder = { Text("Search notes, scans, and more") },
                            leadingIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            trailingIcon = {
                                Row {
                                    if (uiState.query.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.clearSearch() }) {
                                            Icon(
                                                imageVector = Icons.Filled.Clear,
                                                contentDescription = "Clear"
                                            )
                                        }
                                    }
                                    IconButton(onClick = { /* Voice search placeholder */ }) {
                                        Icon(
                                            imageVector = Icons.Filled.Mic,
                                            contentDescription = "Voice search",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        )
                    },
                    expanded = false,
                    onExpandedChange = {},
                    modifier = Modifier.weight(1f)
                ) {}

                Spacer(modifier = Modifier.width(8.dp))

                SmallFloatingActionButton(
                    onClick = {
                        if (uiState.query.isNotBlank()) {
                            viewModel.askAI(uiState.query)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "Ask AI"
                    )
                }
            }

            // Filter chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedFilter == SearchFilter.ALL,
                        onClick = { viewModel.setFilter(SearchFilter.ALL) },
                        label = { Text("All") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedFilter == SearchFilter.VOICE_NOTES,
                        onClick = { viewModel.setFilter(SearchFilter.VOICE_NOTES) },
                        label = { Text("Voice Notes") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedFilter == SearchFilter.SCANNED_DOCS,
                        onClick = { viewModel.setFilter(SearchFilter.SCANNED_DOCS) },
                        label = { Text("Scanned Docs") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedFilter == SearchFilter.ACTION_ITEMS,
                        onClick = { viewModel.setFilter(SearchFilter.ACTION_ITEMS) },
                        label = { Text("Action Items") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedFilter == SearchFilter.CONTACTS,
                        onClick = { viewModel.setFilter(SearchFilter.CONTACTS) },
                        label = { Text("Contacts") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedFilter == SearchFilter.EXPENSES,
                        onClick = { viewModel.setFilter(SearchFilter.EXPENSES) },
                        label = { Text("Expenses") }
                    )
                }
            }

            // Content area
            when {
                uiState.isSearching || uiState.isAskingAI -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            if (uiState.isAskingAI) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Asking AI...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                uiState.query.isEmpty() -> {
                    InitialState()
                }

                else -> {
                    val filter = uiState.selectedFilter
                    val showVoiceNotes = filter == SearchFilter.ALL || filter == SearchFilter.VOICE_NOTES
                    val showScans = filter == SearchFilter.ALL || filter == SearchFilter.SCANNED_DOCS
                    val showActions = filter == SearchFilter.ALL || filter == SearchFilter.ACTION_ITEMS
                    val showContacts = filter == SearchFilter.ALL || filter == SearchFilter.CONTACTS
                    val showExpenses = filter == SearchFilter.ALL || filter == SearchFilter.EXPENSES

                    val voiceNotes = if (showVoiceNotes) uiState.voiceNoteResults else emptyList()
                    val scans = if (showScans) uiState.scanResults else emptyList()
                    val actionItems = if (showActions) uiState.actionItemResults else emptyList()
                    val contacts = if (showContacts) uiState.contactResults else emptyList()
                    val expenses = if (showExpenses) uiState.expenseResults else emptyList()

                    if (voiceNotes.isEmpty() && scans.isEmpty() && actionItems.isEmpty() &&
                        contacts.isEmpty() && expenses.isEmpty() && uiState.aiAnswer == null
                    ) {
                        EmptyResultsState()
                    } else {
                        SearchResultsList(
                            voiceNotes = voiceNotes,
                            scans = scans,
                            actionItems = actionItems,
                            contacts = contacts,
                            expenses = expenses,
                            aiAnswer = uiState.aiAnswer,
                            query = uiState.query,
                            onVoiceNoteClick = { noteId ->
                                navController.navigate(Routes.noteDetail(noteId))
                            },
                            onScanClick = { scanId ->
                                navController.navigate(Routes.scanDetail(scanId))
                            },
                            onDismissAIAnswer = { viewModel.dismissAIAnswer() }
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
    actionItems: List<ActionItem>,
    contacts: List<ScannedContact>,
    expenses: List<Expense>,
    aiAnswer: String?,
    query: String,
    onVoiceNoteClick: (Long) -> Unit,
    onScanClick: (Long) -> Unit,
    onDismissAIAnswer: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // AI Answer card
        if (aiAnswer != null) {
            item(key = "ai_answer") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Answer",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            IconButton(
                                onClick = onDismissAIAnswer,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Dismiss",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = aiAnswer,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        // Voice Notes section
        if (voiceNotes.isNotEmpty()) {
            item(key = "header_voice_notes") {
                ResultSectionHeader("Voice Notes", voiceNotes.size)
            }
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
        }

        // Scanned Documents section
        if (scans.isNotEmpty()) {
            item(key = "header_scans") {
                ResultSectionHeader("Scanned Documents", scans.size)
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

        // Action Items section
        if (actionItems.isNotEmpty()) {
            item(key = "header_action_items") {
                ResultSectionHeader("Action Items", actionItems.size)
            }
            items(
                items = actionItems,
                key = { "action_${it.id}" }
            ) { item ->
                ListItem(
                    headlineContent = {
                        HighlightedText(
                            text = item.title,
                            query = query,
                            maxLines = 1
                        )
                    },
                    supportingContent = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = item.status.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (item.dueDate != null) {
                                Text(
                                    text = "Due: ${DateUtils.formatRelativeTime(item.dueDate)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Action item",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
            }
        }

        // Contacts section
        if (contacts.isNotEmpty()) {
            item(key = "header_contacts") {
                ResultSectionHeader("Contacts", contacts.size)
            }
            items(
                items = contacts,
                key = { "contact_${it.id}" }
            ) { contact ->
                ListItem(
                    headlineContent = {
                        HighlightedText(
                            text = contact.name,
                            query = query,
                            maxLines = 1
                        )
                    },
                    supportingContent = {
                        Column {
                            if (!contact.company.isNullOrBlank()) {
                                HighlightedText(
                                    text = contact.company,
                                    query = query,
                                    maxLines = 1
                                )
                            }
                            if (!contact.email.isNullOrBlank()) {
                                Text(
                                    text = contact.email,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Contact",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
            }
        }

        // Expenses section
        if (expenses.isNotEmpty()) {
            item(key = "header_expenses") {
                ResultSectionHeader("Expenses", expenses.size)
            }
            items(
                items = expenses,
                key = { "expense_${it.id}" }
            ) { expense ->
                ListItem(
                    headlineContent = {
                        HighlightedText(
                            text = expense.merchant,
                            query = query,
                            maxLines = 1
                        )
                    },
                    supportingContent = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "${expense.currency} ${String.format("%.2f", expense.amount)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!expense.category.isNullOrBlank()) {
                                Text(
                                    text = expense.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = DateUtils.formatRelativeTime(expense.date),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.AttachMoney,
                            contentDescription = "Expense",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ResultSectionHeader(title: String, count: Int) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Text(
            text = "$title ($count)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
        )
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Or tap the sparkle button to ask AI a question",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
