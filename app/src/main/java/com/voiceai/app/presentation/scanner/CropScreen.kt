package com.voiceai.app.presentation.scanner

import android.graphics.Bitmap
import android.graphics.PointF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.voiceai.app.presentation.theme.Teal
import com.voiceai.app.util.DocumentScanner
import com.voiceai.app.util.ImageProcessor

private val FILTER_LABELS = listOf("Original", "B&W", "Sharp", "Magic")
private val FILTER_KEYS = listOf("ORIGINAL", "BW", "SHARP", "MAGIC")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropScreen(
    rawImagePath: String,
    detectedCorners: List<Offset>?,
    selectedFilter: String,
    onRetake: () -> Unit,
    onDone: (Bitmap) -> Unit,
    onFilterSelected: (String) -> Unit,
    onCornersUpdated: (List<Offset>) -> Unit
) {
    val originalBitmap = remember(rawImagePath) {
        ImageProcessor.loadBitmapFromFile(rawImagePath)
    }

    if (originalBitmap == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Failed to load image", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var rotation by remember { mutableFloatStateOf(0f) }

    val rotatedBitmap by remember(rotation) {
        derivedStateOf {
            if (rotation != 0f) ImageProcessor.rotateBitmap(originalBitmap, rotation) else originalBitmap
        }
    }

    // Corner positions in normalized coordinates (0..1)
    var topLeft by remember {
        mutableStateOf(
            if (detectedCorners != null && detectedCorners.size == 4) null
            else Offset(0.1f, 0.1f)
        )
    }
    var topRight by remember {
        mutableStateOf(
            if (detectedCorners != null && detectedCorners.size == 4) null
            else Offset(0.9f, 0.1f)
        )
    }
    var bottomLeft by remember {
        mutableStateOf(
            if (detectedCorners != null && detectedCorners.size == 4) null
            else Offset(0.1f, 0.9f)
        )
    }
    var bottomRight by remember {
        mutableStateOf(
            if (detectedCorners != null && detectedCorners.size == 4) null
            else Offset(0.9f, 0.9f)
        )
    }

    // Initialize from detected corners once we have imageSize
    if (topLeft == null && detectedCorners != null && detectedCorners.size == 4 && imageSize.width > 0) {
        val w = imageSize.width.toFloat()
        val h = imageSize.height.toFloat()
        topLeft = Offset(detectedCorners[0].x / w, detectedCorners[0].y / h).coerceNormalized()
        topRight = Offset(detectedCorners[1].x / w, detectedCorners[1].y / h).coerceNormalized()
        bottomRight = Offset(detectedCorners[2].x / w, detectedCorners[2].y / h).coerceNormalized()
        bottomLeft = Offset(detectedCorners[3].x / w, detectedCorners[3].y / h).coerceNormalized()
    }

    val effectiveTopLeft = topLeft ?: Offset(0.1f, 0.1f)
    val effectiveTopRight = topRight ?: Offset(0.9f, 0.1f)
    val effectiveBottomLeft = bottomLeft ?: Offset(0.1f, 0.9f)
    val effectiveBottomRight = bottomRight ?: Offset(0.9f, 0.9f)

    val documentScanner = remember { DocumentScanner() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crop & Enhance") },
                navigationIcon = {
                    IconButton(onClick = onRetake) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retake")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val w = rotatedBitmap.width.toFloat()
                        val h = rotatedBitmap.height.toFloat()
                        val corners = listOf(
                            PointF(effectiveTopLeft.x * w, effectiveTopLeft.y * h),
                            PointF(effectiveTopRight.x * w, effectiveTopRight.y * h),
                            PointF(effectiveBottomRight.x * w, effectiveBottomRight.y * h),
                            PointF(effectiveBottomLeft.x * w, effectiveBottomLeft.y * h)
                        )
                        val cropped = documentScanner.perspectiveTransform(rotatedBitmap, corners)
                        val filterKey = selectedFilter.uppercase().let {
                            if (it == "B&W") "BW" else it
                        }
                        val filtered = documentScanner.applyFilter(cropped, filterKey)
                        onDone(filtered)
                    }) {
                        Icon(Icons.Default.Check, "Done")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Image with crop overlay
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onSizeChanged { imageSize = it }
            ) {
                Image(
                    bitmap = rotatedBitmap.asImageBitmap(),
                    contentDescription = "Captured document",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Crop overlay: lines and corner handles
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val points = listOf(
                        Offset(effectiveTopLeft.x * w, effectiveTopLeft.y * h),
                        Offset(effectiveTopRight.x * w, effectiveTopRight.y * h),
                        Offset(effectiveBottomRight.x * w, effectiveBottomRight.y * h),
                        Offset(effectiveBottomLeft.x * w, effectiveBottomLeft.y * h)
                    )

                    // Semi-transparent fill inside crop region
                    val path = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        points.drop(1).forEach { lineTo(it.x, it.y) }
                        close()
                    }
                    drawPath(
                        path = path,
                        color = Teal.copy(alpha = 0.08f)
                    )

                    // Connecting lines
                    for (i in points.indices) {
                        drawLine(
                            color = Teal,
                            start = points[i],
                            end = points[(i + 1) % points.size],
                            strokeWidth = 3.dp.toPx()
                        )
                    }

                    // Corner handles
                    points.forEach { point ->
                        drawCircle(
                            color = Teal,
                            radius = 12.dp.toPx(),
                            center = point,
                            style = Stroke(width = 3.dp.toPx())
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 8.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = Teal,
                            radius = 4.dp.toPx(),
                            center = point
                        )
                    }
                }

                // Draggable handle areas for each corner
                val cornerSetters = listOf(
                    effectiveTopLeft to { offset: Offset ->
                        topLeft = offset
                    },
                    effectiveTopRight to { offset: Offset ->
                        topRight = offset
                    },
                    effectiveBottomRight to { offset: Offset ->
                        bottomRight = offset
                    },
                    effectiveBottomLeft to { offset: Offset ->
                        bottomLeft = offset
                    }
                )

                cornerSetters.forEach { (point, setter) ->
                    Box(
                        modifier = Modifier
                            .offset(
                                x = (point.x * imageSize.width).pxToDp() - 20.dp,
                                y = (point.y * imageSize.height).pxToDp() - 20.dp
                            )
                            .size(40.dp)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val newX = (point.x + dragAmount.x / imageSize.width)
                                        .coerceIn(0f, 1f)
                                    val newY = (point.y + dragAmount.y / imageSize.height)
                                        .coerceIn(0f, 1f)
                                    setter(Offset(newX, newY))
                                }
                            }
                    )
                }
            }

            // Rotate buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { rotation -= 90f }) {
                    Icon(Icons.Default.RotateLeft, "Rotate Left")
                }
                Spacer(modifier = Modifier.width(24.dp))
                IconButton(onClick = { rotation += 90f }) {
                    Icon(Icons.Default.RotateRight, "Rotate Right")
                }
            }

            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FILTER_LABELS.forEachIndexed { index, label ->
                    val key = FILTER_KEYS[index]
                    FilterChip(
                        selected = selectedFilter.equals(key, ignoreCase = true),
                        onClick = { onFilterSelected(key) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}

private fun Offset.coerceNormalized(): Offset =
    Offset(x.coerceIn(0f, 1f), y.coerceIn(0f, 1f))

@Composable
private fun Float.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }
