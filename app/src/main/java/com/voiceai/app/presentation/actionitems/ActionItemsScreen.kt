package com.voiceai.app.presentation.actionitems

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.voiceai.app.domain.model.ActionItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val tabTitles = listOf("To Do", "In Progress", "Done")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionItemsScreen(
    navController: NavController,
    viewModel: ActionItemsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Action Items") },
                actions = {
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(
                                imageVector = Icons.Filled.FilterList,
                                contentDescription = "Filter"
                            )
                        }
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            PriorityFilter.entries.forEach { filter ->
                                DropdownMenuItem(
                                    text = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        viewModel.setFilter(filter)
                                        showFilterMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add action item"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedTab,
                edgePadding = 16.dp
            ) {
                tabTitles.forEachIndexed { index, title ->
                    val count = when (index) {
                        0 -> uiState.todoItems.size
                        1 -> uiState.inProgressItems.size
                        2 -> uiState.doneItems.size
                        else -> 0
                    }
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = { Text("$title ($count)") }
                    )
                }
            }

            val currentItems = when (uiState.selectedTab) {
                0 -> uiState.todoItems
                1 -> uiState.inProgressItems
                2 -> uiState.doneItems
                else -> emptyList()
            }

            if (currentItems.isEmpty() && !uiState.isLoading) {
                EmptyTabState(tabTitle = tabTitles[uiState.selectedTab])
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = currentItems,
                        key = { it.id }
                    ) { item ->
                        SwipeableActionItemCard(
                            item = item,
                            currentTab = uiState.selectedTab,
                            onSwipeToNext = { nextStatus ->
                                viewModel.updateStatus(item.id, nextStatus)
                            },
                            onTap = {
                                // Navigate to source note/scan
                                item.sourceNoteId?.let { noteId ->
                                    navController.navigate("detail/$noteId")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddActionItemDialog(
            onDismiss = { viewModel.dismissAddDialog() },
            onAdd = { title, priority, dueDate ->
                viewModel.addItem(title, priority, dueDate)
                viewModel.dismissAddDialog()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableActionItemCard(
    item: ActionItem,
    currentTab: Int,
    onSwipeToNext: (String) -> Unit,
    onTap: () -> Unit
) {
    val nextStatus = when (currentTab) {
        0 -> "in_progress"
        1 -> "done"
        else -> null
    }

    if (nextStatus != null) {
        val dismissState = rememberSwipeToDismissBoxState()

        LaunchedEffect(dismissState.currentValue) {
            if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart ||
                dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd
            ) {
                onSwipeToNext(nextStatus)
            }
        }

        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val color by animateColorAsState(
                    targetValue = when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.surfaceVariant
                        else -> MaterialTheme.colorScheme.primaryContainer
                    },
                    label = "swipeBg"
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color, RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Move to $nextStatus",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentTab == 0) "Start" else "Complete",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            },
            enableDismissFromEndToStart = true,
            enableDismissFromStartToEnd = true
        ) {
            ActionItemCard(item = item, onTap = onTap)
        }
    } else {
        ActionItemCard(item = item, onTap = onTap)
    }
}

@Composable
private fun ActionItemCard(
    item: ActionItem,
    onTap: () -> Unit
) {
    val isOverdue = item.dueDate != null &&
            item.dueDate < System.currentTimeMillis() &&
            item.status != "done"

    val priorityColor = when (item.priority) {
        3 -> Color(0xFFE53935) // Red - high
        2 -> Color(0xFFFDD835) // Yellow - medium
        else -> Color(0xFF43A047) // Green - low
    }

    val priorityLabel = when (item.priority) {
        3 -> "High"
        2 -> "Medium"
        else -> "Low"
    }

    Card(
        onClick = onTap,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = if (isOverdue) BorderStroke(2.dp, Color(0xFFE53935)) else null,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Priority colored strip
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .height(72.dp)
                    .background(priorityColor, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (item.status == "done") {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF43A047),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$priorityLabel priority",
                        style = MaterialTheme.typography.labelSmall,
                        color = priorityColor
                    )

                    if (item.dueDate != null) {
                        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        Text(
                            text = if (isOverdue) "Overdue: ${dateFormat.format(Date(item.dueDate))}"
                            else "Due: ${dateFormat.format(Date(item.dueDate))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverdue) Color(0xFFE53935)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (item.sourceNoteId != null || item.sourceScanId != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (item.sourceNoteId != null) "From voice note" else "From scan",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTabState(tabTitle: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Inbox,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No items in \"$tabTitle\"",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun AddActionItemDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, priority: Int, dueDate: Long?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableIntStateOf(2) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Action Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1 to "Low", 2 to "Medium", 3 to "High").forEach { (value, label) ->
                        OutlinedButton(
                            onClick = { selectedPriority = value },
                            border = BorderStroke(
                                1.dp,
                                if (selectedPriority == value) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Text(
                                text = label,
                                color = if (selectedPriority == value) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(title, selectedPriority, null) },
                enabled = title.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
