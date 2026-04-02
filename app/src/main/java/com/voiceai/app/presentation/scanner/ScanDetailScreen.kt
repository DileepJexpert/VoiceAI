package com.voiceai.app.presentation.scanner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.voiceai.app.domain.model.ScannedPage
import kotlinx.coroutines.launch

private val ScanTabs = listOf("Scan", "Text", "Summary")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDetailScreen(
    navController: NavController,
    viewModel: ScanDetailViewModel = hiltViewModel()
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
            title = { Text("Delete Document") },
            text = {
                Text("Are you sure you want to delete this scanned document? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteDocument() }) {
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
                    val document = uiState.document
                    if (document != null) {
                        var isEditing by remember { mutableStateOf(false) }
                        var editingTitle by remember(document.title) {
                            mutableStateOf(document.title)
                        }

                        if (isEditing) {
                            OutlinedTextField(
                                value = editingTitle,
                                onValueChange = { editingTitle = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(
                                text = document.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        LaunchedEffect(isEditing) {
                            if (!isEditing && editingTitle != document.title) {
                                viewModel.updateTitle(editingTitle)
                            }
                        }
                    } else {
                        Text(
                            text = "Scan Detail",
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
                    val document = uiState.document
                    if (document != null) {
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (document.isFavorite) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Filled.FavoriteBorder
                                },
                                contentDescription = if (document.isFavorite) {
                                    "Remove from favorites"
                                } else {
                                    "Add to favorites"
                                },
                                tint = if (document.isFavorite) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }

                        IconButton(onClick = { /* Share intent integration */ }) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share"
                            )
                        }

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
            if (uiState.isTtsSpeaking) {
                TtsMiniPlayer(
                    progress = uiState.ttsProgress,
                    speed = uiState.ttsSpeed,
                    onPlayPause = { viewModel.toggleTts() },
                    onSpeedChange = { viewModel.setTtsSpeed(it) },
                    onClose = { viewModel.stopTts() }
                )
            } else if (uiState.document != null) {
                BottomAppBar(tonalElevation = 4.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = { viewModel.exportPdf() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PDF", style = MaterialTheme.typography.labelMedium)
                        }

                        FilledTonalButton(
                            onClick = { /* Share as image integration */ },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Image,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Image", style = MaterialTheme.typography.labelMedium)
                        }

                        FilledTonalButton(
                            onClick = {
                                val text = uiState.document?.extractedText ?: ""
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
                            Text("Copy", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (!uiState.isTtsSpeaking && uiState.document != null) {
                FloatingActionButton(onClick = { viewModel.startTts() }) {
                    Icon(
                        imageVector = Icons.Filled.VolumeUp,
                        contentDescription = "Read Aloud"
                    )
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
            visible = !uiState.isLoading && uiState.document != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val document = uiState.document ?: return@AnimatedVisibility

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                TabRow(selectedTabIndex = uiState.selectedTab) {
                    ScanTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = uiState.selectedTab == index,
                            onClick = { viewModel.selectTab(index) },
                            text = { Text(title) }
                        )
                    }
                }

                when (uiState.selectedTab) {
                    0 -> ScanTab(
                        pages = uiState.pages,
                        currentPageIndex = uiState.currentPageIndex,
                        onPageChanged = { viewModel.setCurrentPage(it) }
                    )
                    1 -> TextTab(
                        extractedText = document.extractedText,
                        onCopyAll = {
                            clipboardManager.setText(
                                AnnotatedString(document.extractedText ?: "")
                            )
                        }
                    )
                    2 -> SummaryTab(
                        summary = document.summary,
                        language = document.language
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !uiState.isLoading && uiState.document == null,
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
                    text = "Document not found",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ScanTab(
    pages: List<ScannedPage>,
    currentPageIndex: Int,
    onPageChanged: (Int) -> Unit
) {
    if (pages.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No pages available",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = currentPageIndex,
        pageCount = { pages.size }
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage)
    }

    LaunchedEffect(currentPageIndex) {
        if (pagerState.currentPage != currentPageIndex) {
            scope.launch { pagerState.animateScrollToPage(currentPageIndex) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { pageIndex ->
            ZoomableImage(imagePath = pages[pageIndex].imagePath)
        }

        if (pages.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                pages.forEachIndexed { index, _ ->
                    val isSelected = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "${pagerState.currentPage + 1} / ${pages.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ZoomableImage(imagePath: String) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    if (scale > 1f) {
                        offsetX += pan.x
                        offsetY += pan.y
                    } else {
                        offsetX = 0f
                        offsetY = 0f
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imagePath,
            contentDescription = "Scanned page",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
        )
    }
}

@Composable
private fun TextTab(
    extractedText: String?,
    onCopyAll: () -> Unit
) {
    if (extractedText.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No text extracted",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            FilledTonalButton(
                onClick = onCopyAll,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy All", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        var editableText by remember(extractedText) { mutableStateOf(extractedText) }
        OutlinedTextField(
            value = editableText,
            onValueChange = { editableText = it },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge,
            label = { Text("Extracted Text") }
        )
    }
}

@Composable
private fun SummaryTab(
    summary: String?,
    language: String
) {
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
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        AssistChip(
            onClick = {},
            label = {
                Text(
                    text = language.uppercase(),
                    style = MaterialTheme.typography.labelSmall
                )
            },
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Text(
            text = "Key Points",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val keyPoints = remember(summary) { parseSummaryPoints(summary) }

        if (keyPoints.isNotEmpty()) {
            keyPoints.forEach { point ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "\u2022",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
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
        } else {
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TtsMiniPlayer(
    progress: Float,
    speed: Float,
    onPlayPause: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onClose: () -> Unit
) {
    var showSpeedMenu by remember { mutableStateOf(false) }
    val speedOptions = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = Icons.Filled.Pause,
                    contentDescription = "Pause",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "Reading aloud...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Box {
                IconButton(onClick = { showSpeedMenu = true }) {
                    Text(
                        text = "${speed}x",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = showSpeedMenu,
                    onDismissRequest = { showSpeedMenu = false }
                ) {
                    speedOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text("${option}x") },
                            onClick = {
                                onSpeedChange(option)
                                showSpeedMenu = false
                            }
                        )
                    }
                }
            }

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Stop reading",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun parseSummaryPoints(summary: String): List<String> {
    val lines = summary.lines().map { it.trim() }.filter { it.isNotBlank() }
    val bulletPattern = Regex("""^[-*\u2022]\s+(.+)""")
    val numberedPattern = Regex("""^\d+[.)]\s*(.+)""")

    val points = lines.mapNotNull { line ->
        bulletPattern.matchEntire(line)?.groupValues?.getOrNull(1)
            ?: numberedPattern.matchEntire(line)?.groupValues?.getOrNull(1)
    }

    return points.ifEmpty {
        summary.split(Regex("""(?<=[.!?])\s+"""))
            .map { it.trim() }
            .filter { it.isNotBlank() && it.length > 10 }
    }
}
