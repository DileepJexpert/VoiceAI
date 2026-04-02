package com.voiceai.app.presentation.record

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.voiceai.app.presentation.components.WaveformView
import com.voiceai.app.presentation.navigation.Routes
import com.voiceai.app.presentation.theme.Coral
import com.voiceai.app.presentation.theme.CoralDark
import com.voiceai.app.presentation.theme.CoralLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    navController: NavController,
    viewModel: RecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val permissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showSaveSheet by remember { mutableStateOf(false) }

    val hasRecordingData = uiState.duration > 0L && !uiState.isRecording && !uiState.isPaused

    // Navigate on save completion
    LaunchedEffect(uiState.savedNoteId) {
        uiState.savedNoteId?.let { noteId ->
            navController.navigate(Routes.noteDetail(noteId)) {
                popUpTo(Routes.RECORD) { inclusive = true }
            }
        }
    }

    // Show save sheet when recording stops and there is data
    LaunchedEffect(hasRecordingData) {
        if (hasRecordingData && uiState.savedNoteId == null) {
            showSaveSheet = true
        }
    }

    // Back handler to confirm discard
    BackHandler(enabled = uiState.isRecording || uiState.isPaused || hasRecordingData) {
        showDiscardDialog = true
    }

    // Discard confirmation dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard Recording?") },
            text = { Text("Your current recording will be lost. Are you sure you want to go back?") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    viewModel.discardRecording()
                    navController.popBackStack()
                }) {
                    Text("Discard", color = CoralDark)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.isRecording || uiState.isPaused || hasRecordingData) {
                            showDiscardDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!permissionState.status.isGranted) {
                // Permission not granted UI
                PermissionRationale(
                    shouldShowRationale = permissionState.status.shouldShowRationale,
                    onRequestPermission = { permissionState.launchPermissionRequest() }
                )
            } else {
                // Main recording UI
                RecordingContent(
                    uiState = uiState,
                    onStartRecording = viewModel::startRecording,
                    onPauseRecording = viewModel::pauseRecording,
                    onResumeRecording = viewModel::resumeRecording,
                    onStopRecording = viewModel::stopRecording
                )
            }

            // Loading overlay
            AnimatedVisibility(
                visible = uiState.isSaving || uiState.isTranscribing,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Coral)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (uiState.isSaving) "Saving..." else "Transcribing...",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }

    // Save bottom sheet
    if (showSaveSheet) {
        SaveRecordingSheet(
            onSave = { title ->
                showSaveSheet = false
                viewModel.saveNote(title)
            },
            onDiscard = {
                showSaveSheet = false
                viewModel.discardRecording()
                navController.popBackStack()
            },
            onReRecord = {
                showSaveSheet = false
                viewModel.discardRecording()
            }
        )
    }
}

@Composable
private fun PermissionRationale(
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Mic,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Coral
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Microphone Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (shouldShowRationale) {
                "VoiceAI needs microphone access to record audio notes. Please grant the permission to continue."
            } else {
                "To record voice notes, VoiceAI needs access to your microphone."
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = Coral)
        ) {
            Text("Grant Permission")
        }
    }
}

@Composable
private fun RecordingContent(
    uiState: RecordUiState,
    onStartRecording: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    val isActive = uiState.isRecording && !uiState.isPaused

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Pulsing circle with mic icon
        PulsingMicCircle(isActive = isActive)

        Spacer(modifier = Modifier.height(32.dp))

        // Waveform visualization
        WaveformView(
            amplitudes = uiState.amplitudes,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            barColor = Coral
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Timer display
        Text(
            text = formatDuration(uiState.duration),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Light,
                letterSpacing = 2.sp
            ),
            color = if (isActive) Coral else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.weight(1f))

        // Control buttons
        ControlButtons(
            isRecording = uiState.isRecording,
            isPaused = uiState.isPaused,
            onStartRecording = onStartRecording,
            onPauseRecording = onPauseRecording,
            onResumeRecording = onResumeRecording,
            onStopRecording = onStopRecording
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun PulsingMicCircle(isActive: Boolean) {
    val infiniteTransition: InfiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val scale by animateFloatAsState(
        targetValue = if (isActive) pulseScale else 1f,
        label = "circleScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isActive) 0.15f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "outerAlpha"
    )

    Box(contentAlignment = Alignment.Center) {
        // Outer glow circle
        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(scale)
                .graphicsLayer { this.alpha = alpha }
                .background(color = Coral, shape = CircleShape)
        )

        // Main circle
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .background(
                    color = if (isActive) Coral else CoralLight,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = "Microphone",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun ControlButtons(
    isRecording: Boolean,
    isPaused: Boolean,
    onStartRecording: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            // Not yet recording
            !isRecording && !isPaused -> {
                FilledIconButton(
                    onClick = onStartRecording,
                    modifier = Modifier.size(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Coral,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Start Recording",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            // Currently recording (not paused)
            isRecording && !isPaused -> {
                FilledIconButton(
                    onClick = onPauseRecording,
                    modifier = Modifier.size(64.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.Pause,
                        contentDescription = "Pause Recording",
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                FilledIconButton(
                    onClick = onStopRecording,
                    modifier = Modifier.size(64.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = CoralDark,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "Stop Recording",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            // Paused
            isPaused -> {
                FilledIconButton(
                    onClick = onResumeRecording,
                    modifier = Modifier.size(64.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Coral,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Resume Recording",
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                FilledIconButton(
                    onClick = onStopRecording,
                    modifier = Modifier.size(64.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = CoralDark,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "Stop Recording",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaveRecordingSheet(
    onSave: (String) -> Unit,
    onDiscard: () -> Unit,
    onReRecord: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val defaultTitle = remember {
        val timestamp = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            .format(Date())
        "Recording $timestamp"
    }
    var title by rememberSaveable { mutableStateOf(defaultTitle) }

    ModalBottomSheet(
        onDismissRequest = { /* Prevent accidental dismiss */ },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Save Recording",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onSave(title) },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Coral),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Save",
                    modifier = Modifier.padding(vertical = 4.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onReRecord,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Re-record",
                    modifier = Modifier.padding(vertical = 4.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onDiscard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = CoralDark
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Discard",
                    color = CoralDark,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
