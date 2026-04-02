package com.voiceai.app.presentation.folders

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.voiceai.app.domain.model.Folder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(
    navController: NavController,
    onFolderClick: (Folder) -> Unit = {},
    viewModel: FoldersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Create folder dialog
    if (uiState.showCreateDialog) {
        CreateFolderDialog(
            onDismiss = { viewModel.dismissCreateDialog() },
            onCreate = { name, icon -> viewModel.createFolder(name, icon) }
        )
    }

    // Edit folder dialog
    if (uiState.showEditDialog && uiState.editingFolder != null) {
        EditFolderDialog(
            folder = uiState.editingFolder!!,
            onDismiss = { viewModel.dismissEditDialog() },
            onUpdate = { name, icon -> viewModel.updateFolder(uiState.editingFolder!!, name, icon) },
            onDelete = {
                viewModel.deleteFolder(uiState.editingFolder!!)
                viewModel.dismissEditDialog()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Folders") },
                actions = {
                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(
                            imageVector = if (uiState.viewMode == FolderViewMode.GRID)
                                Icons.Filled.ViewList else Icons.Filled.GridView,
                            contentDescription = "Toggle view"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateDialog() },
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create Folder")
            }
        }
    ) { innerPadding ->
        val smartFolders = uiState.folders.filter { it.isSmartFolder }
        val userFolders = uiState.folders.filter { !it.isSmartFolder }

        when (uiState.viewMode) {
            FolderViewMode.GRID -> {
                GridFoldersView(
                    smartFolders = smartFolders,
                    userFolders = userFolders,
                    onFolderClick = onFolderClick,
                    onFolderLongPress = { folder ->
                        if (!folder.isSmartFolder) viewModel.showEditDialog(folder)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            FolderViewMode.LIST -> {
                ListFoldersView(
                    smartFolders = smartFolders,
                    userFolders = userFolders,
                    onFolderClick = onFolderClick,
                    onFolderLongPress = { folder ->
                        if (!folder.isSmartFolder) viewModel.showEditDialog(folder)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridFoldersView(
    smartFolders: List<Folder>,
    userFolders: List<Folder>,
    onFolderClick: (Folder) -> Unit,
    onFolderLongPress: (Folder) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Smart Folders header
        item(span = { GridItemSpan(2) }) {
            SectionHeader(title = "Smart Folders", subtitle = "AI auto-sorted")
        }

        items(smartFolders) { folder ->
            FolderGridCard(
                folder = folder,
                onClick = { onFolderClick(folder) },
                onLongClick = { onFolderLongPress(folder) }
            )
        }

        // User Folders header
        if (userFolders.isNotEmpty()) {
            item(span = { GridItemSpan(2) }) {
                SectionHeader(title = "Your Folders")
            }

            items(userFolders) { folder ->
                FolderGridCard(
                    folder = folder,
                    onClick = { onFolderClick(folder) },
                    onLongClick = { onFolderLongPress(folder) }
                )
            }
        }

        // Bottom spacer for FAB
        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListFoldersView(
    smartFolders: List<Folder>,
    userFolders: List<Folder>,
    onFolderClick: (Folder) -> Unit,
    onFolderLongPress: (Folder) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // Smart Folders header
        item {
            SectionHeader(
                title = "Smart Folders",
                subtitle = "AI auto-sorted",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(smartFolders) { folder ->
            ListItem(
                headlineContent = {
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                leadingContent = {
                    Text(
                        text = folder.icon,
                        fontSize = 24.sp
                    )
                },
                modifier = Modifier.combinedClickable(
                    onClick = { onFolderClick(folder) },
                    onLongClick = { onFolderLongPress(folder) }
                )
            )
        }

        // User Folders header
        if (userFolders.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Your Folders",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(userFolders) { folder ->
                ListItem(
                    headlineContent = {
                        Text(
                            text = folder.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    leadingContent = {
                        Text(
                            text = folder.icon,
                            fontSize = 24.sp
                        )
                    },
                    modifier = Modifier.combinedClickable(
                        onClick = { onFolderClick(folder) },
                        onLongClick = { onFolderLongPress(folder) }
                    )
                )
            }
        }

        // Bottom spacer for FAB
        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderGridCard(
    folder: Folder,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (folder.isSmartFolder)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = folder.icon,
                fontSize = 36.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = folder.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, icon: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("\uD83D\uDCC1") }

    val emojiOptions = listOf(
        "\uD83D\uDCC1", "\uD83D\uDCC2", "\u2B50", "\u2764\uFE0F", "\uD83C\uDFAF",
        "\uD83D\uDE80", "\uD83C\uDFB5", "\uD83D\uDCDA", "\uD83D\uDCBB", "\uD83C\uDF1F",
        "\uD83D\uDD25", "\uD83C\uDF08", "\uD83C\uDFE0", "\uD83D\uDEE0\uFE0F", "\uD83C\uDFA8",
        "\uD83E\uDDE0", "\uD83D\uDCAC", "\uD83D\uDCCC", "\uD83D\uDD0D", "\u2705"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Folder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Choose Icon",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                EmojiPicker(
                    emojis = emojiOptions,
                    selectedEmoji = selectedIcon,
                    onEmojiSelected = { selectedIcon = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onCreate(name.trim(), selectedIcon) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EditFolderDialog(
    folder: Folder,
    onDismiss: () -> Unit,
    onUpdate: (name: String, icon: String) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(folder.name) }
    var selectedIcon by remember { mutableStateOf(folder.icon) }

    val emojiOptions = listOf(
        "\uD83D\uDCC1", "\uD83D\uDCC2", "\u2B50", "\u2764\uFE0F", "\uD83C\uDFAF",
        "\uD83D\uDE80", "\uD83C\uDFB5", "\uD83D\uDCDA", "\uD83D\uDCBB", "\uD83C\uDF1F",
        "\uD83D\uDD25", "\uD83C\uDF08", "\uD83C\uDFE0", "\uD83D\uDEE0\uFE0F", "\uD83C\uDFA8",
        "\uD83E\uDDE0", "\uD83D\uDCAC", "\uD83D\uDCCC", "\uD83D\uDD0D", "\u2705"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Folder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Choose Icon",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                EmojiPicker(
                    emojis = emojiOptions,
                    selectedEmoji = selectedIcon,
                    onEmojiSelected = { selectedIcon = it }
                )

                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Delete Folder",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onUpdate(name.trim(), selectedIcon) },
                enabled = name.isNotBlank()
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EmojiPicker(
    emojis: List<String>,
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(emojis) { emoji ->
            Card(
                modifier = Modifier
                    .size(48.dp)
                    .combinedClickable(onClick = { onEmojiSelected(emoji) }),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (emoji == selectedEmoji)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 24.sp)
                }
            }
        }
    }
}
