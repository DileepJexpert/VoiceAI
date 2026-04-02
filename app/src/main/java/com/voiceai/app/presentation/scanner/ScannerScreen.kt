package com.voiceai.app.presentation.scanner

import android.Manifest
import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voiceai.app.util.FileUtils
import com.voiceai.app.util.Constants
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    navController: androidx.navigation.NavController,
    viewModel: ScannerViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigate to scan detail when saved
    LaunchedEffect(uiState.savedDocumentId) {
        uiState.savedDocumentId?.let { id ->
            navController.navigate(com.voiceai.app.presentation.navigation.Routes.scanDetail(id)) {
                popUpTo(com.voiceai.app.presentation.navigation.Routes.SCANNER) { inclusive = true }
            }
        }
    }

    when (uiState.currentStep) {
        ScanStep.CAMERA -> CameraCaptureScreen(
            flashEnabled = uiState.flashEnabled,
            autoCaptureEnabled = uiState.autoCaptureEnabled,
            detectedCorners = uiState.detectedCorners,
            onImageCaptured = { viewModel.onImageCaptured(it) },
            onToggleFlash = { viewModel.toggleFlash() },
            onBack = { navController.popBackStack() }
        )
        ScanStep.CROP -> CropScreen(
            rawImagePath = viewModel.getCurrentRawImagePath() ?: "",
            detectedCorners = uiState.detectedCorners,
            selectedFilter = uiState.selectedFilter,
            onRetake = { viewModel.goBackToCamera() },
            onDone = { bitmap -> viewModel.onCropConfirmed(bitmap) },
            onFilterSelected = { viewModel.setFilter(it) },
            onCornersUpdated = { viewModel.updateCorners(it) }
        )
        ScanStep.REVIEW, ScanStep.PROCESSING -> ScanReviewScreen(
            pages = uiState.capturedPages,
            isProcessing = uiState.isProcessing,
            processingStep = uiState.processingStep,
            onAddPage = { viewModel.addPage() },
            onRemovePage = { viewModel.removePage(it) },
            onProcessAndSave = { viewModel.processAndSave(it) },
            onBack = { viewModel.goBackToCamera() }
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CameraCaptureScreen(
    flashEnabled: Boolean,
    autoCaptureEnabled: Boolean,
    detectedCorners: List<Offset>?,
    onImageCaptured: (String) -> Unit,
    onToggleFlash: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    if (!cameraPermissionState.status.isGranted) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Camera permission is required to scan documents.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        return
    }

    val imageCapture = remember { ImageCapture.Builder().build() }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = FileUtils.generateFileName("import", Constants.IMAGE_FORMAT)
            val filePath = FileUtils.getScanFilePath(context, fileName)
            copyUriToFile(context, it, filePath)
            onImageCaptured(filePath)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val provider = cameraProviderFuture.get()
                    cameraProvider = provider

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (_: Exception) {
                        // Camera binding failed
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Document edge overlay
        DocumentEdgeOverlay(
            corners = detectedCorners,
            modifier = Modifier.fillMaxSize()
        )

        // Guide text at top
        Text(
            text = "Position document within frame",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Auto-capture indicator
        if (autoCaptureEnabled && detectedCorners != null) {
            AutoCaptureIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 112.dp)
            )
        }

        // Bottom bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gallery import button
            IconButton(
                onClick = { galleryLauncher.launch("image/*") }
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Import from gallery",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Capture button
            CaptureButton(
                onClick = {
                    val fileName = FileUtils.generateFileName("scan", Constants.IMAGE_FORMAT)
                    val filePath = FileUtils.getScanFilePath(context, fileName)
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(File(filePath)).build()
                    imageCapture.flashMode = if (flashEnabled) {
                        ImageCapture.FLASH_MODE_ON
                    } else {
                        ImageCapture.FLASH_MODE_OFF
                    }
                    imageCapture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                onImageCaptured(filePath)
                            }
                            override fun onError(exception: ImageCaptureException) {
                                // Capture failed
                            }
                        }
                    )
                }
            )

            // Flash toggle
            IconButton(
                onClick = onToggleFlash
            ) {
                Icon(
                    imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = if (flashEnabled) "Turn off flash" else "Turn on flash",
                    tint = if (flashEnabled) Color.Yellow else Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll()
        }
    }
}

@Composable
private fun CaptureButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .border(4.dp, Color.White, CircleShape)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .background(Color.White, CircleShape)
        ) {
            // Empty content for a solid white circle button
        }
    }
}

@Composable
private fun DocumentEdgeOverlay(
    corners: List<Offset>?,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "edgePulse")
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cornerAlpha"
    )

    Canvas(modifier = modifier) {
        if (corners == null || corners.size < 4) return@Canvas

        val edgeColor = Color(0xFF00BFA5).copy(alpha = animatedAlpha)
        val cornerLength = 40.dp.toPx()

        // Draw connecting lines
        val path = Path().apply {
            moveTo(corners[0].x, corners[0].y)
            lineTo(corners[1].x, corners[1].y)
            lineTo(corners[2].x, corners[2].y)
            lineTo(corners[3].x, corners[3].y)
            close()
        }
        drawPath(
            path = path,
            color = edgeColor.copy(alpha = 0.3f),
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw corner indicators
        corners.forEachIndexed { index, corner ->
            val nextCorner = corners[(index + 1) % 4]
            val prevCorner = corners[(index + 3) % 4]

            // Direction vectors toward adjacent corners
            val toNext = Offset(nextCorner.x - corner.x, nextCorner.y - corner.y).normalized(cornerLength)
            val toPrev = Offset(prevCorner.x - corner.x, prevCorner.y - corner.y).normalized(cornerLength)

            // Draw corner lines
            drawLine(
                color = edgeColor,
                start = corner,
                end = Offset(corner.x + toNext.x, corner.y + toNext.y),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = edgeColor,
                start = corner,
                end = Offset(corner.x + toPrev.x, corner.y + toPrev.y),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Draw corner circle
            drawCircle(
                color = edgeColor,
                radius = 6.dp.toPx(),
                center = corner
            )
        }
    }
}

@Composable
private fun AutoCaptureIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "autoCapture")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = Constants.AUTO_CAPTURE_DELAY_MS.toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "autoCaptureProgress"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hold steady...",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.3f), MaterialTheme.shapes.small)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(4.dp)
                    .background(Color(0xFF00BFA5), MaterialTheme.shapes.small)
            )
        }
    }
}

private fun Offset.normalized(length: Float): Offset {
    val magnitude = kotlin.math.sqrt(x * x + y * y)
    if (magnitude == 0f) return Offset.Zero
    return Offset(x / magnitude * length, y / magnitude * length)
}

private fun copyUriToFile(context: Context, uri: Uri, outputPath: String) {
    try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            File(outputPath).outputStream().use { output ->
                input.copyTo(output)
            }
        }
    } catch (_: Exception) {
        // Copy failed
    }
}
