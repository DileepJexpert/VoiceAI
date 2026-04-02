package com.voiceai.app.presentation.scanner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voiceai.app.presentation.theme.Teal
import com.voiceai.app.util.ImageProcessor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ScanReviewScreen(
    pages: List<CapturedPage>,
    isProcessing: Boolean,
    processingStep: String,
    onAddPage: () -> Unit,
    onRemovePage: (Int) -> Unit,
    onProcessAndSave: (String) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    var deleteConfirmIndex by remember { mutableIntStateOf(-1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Scan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { onProcessAndSave(title) },
                        enabled = pages.isNotEmpty() && !isProcessing
                    ) {
                        Text("Process & Save")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Title text field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Document title") },
                    placeholder = { Text("Untitled Scan") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    textStyle = MaterialTheme.typography.titleMedium
                )

                // Page counter
                if (pages.isNotEmpty()) {
                    Text(
                        text = "Page ${pagerState.currentPage + 1} of ${pages.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 4.dp)
                    )
                }

                // Main page viewer (swipeable)
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { pageIndex ->
                    val page = pages[pageIndex]
                    val bitmap = remember(page.imagePath, page.processedBitmap) {
                        page.processedBitmap ?: ImageProcessor.loadBitmapFromFile(page.imagePath)
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Page ${pageIndex + 1}",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Failed to load page")
                        }
                    }
                }

                // Thumbnail strip
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(pages) { index, page ->
                        Box {
                            val bitmap = remember(page.imagePath, page.processedBitmap) {
                                page.processedBitmap ?: ImageProcessor.loadBitmapFromFile(page.imagePath)
                            }
                            val isSelected = pagerState.currentPage == index

                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Teal else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .combinedClickable(
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        },
                                        onLongClick = {
                                            deleteConfirmIndex = index
                                        }
                                    )
                            ) {
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Page ${index + 1} thumbnail",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            // Page number badge
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(2.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    // Add page button
                    item {
                        IconButton(
                            onClick = onAddPage,
                            modifier = Modifier
                                .size(64.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Icon(Icons.Default.Add, "Add page")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Delete confirmation dialog (on long-press)
            if (deleteConfirmIndex >= 0) {
                AlertDialog(
                    onDismissRequest = { deleteConfirmIndex = -1 },
                    title = { Text("Remove page?") },
                    text = { Text("Remove page ${deleteConfirmIndex + 1} from this scan?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onRemovePage(deleteConfirmIndex)
                                deleteConfirmIndex = -1
                            }
                        ) {
                            Text("Remove", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { deleteConfirmIndex = -1 }) {
                            Text("Cancel")
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            }

            // Processing overlay
            AnimatedVisibility(
                visible = isProcessing,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300)),
                modifier = Modifier.fillMaxSize()
            ) {
                ProcessingOverlay(processingStep = processingStep)
            }
        }
    }
}

@Composable
private fun ProcessingOverlay(processingStep: String) {
    val steps = listOf(
        "Enhancing images...",
        "Extracting text (OCR)...",
        "Generating AI summary..."
    )
    val currentStepIndex = steps.indexOf(processingStep)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(0.85f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Processing Document",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                steps.forEachIndexed { index, step ->
                    val isCompleted = currentStepIndex > index
                    val isCurrent = currentStepIndex == index

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when {
                            isCompleted -> {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Completed",
                                    tint = Teal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            isCurrent -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Teal
                                )
                            }
                            else -> {
                                Icon(
                                    Icons.Outlined.Circle,
                                    contentDescription = "Pending",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                isCompleted -> Teal
                                isCurrent -> MaterialTheme.colorScheme.onSurface
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            },
                            fontWeight = if (isCurrent) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Overall progress indicator
                val progress by animateFloatAsState(
                    targetValue = when {
                        currentStepIndex < 0 -> 0f
                        currentStepIndex >= steps.size -> 1f
                        else -> (currentStepIndex + 0.5f) / steps.size
                    },
                    animationSpec = tween(600),
                    label = "processingProgress"
                )

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Teal,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}
