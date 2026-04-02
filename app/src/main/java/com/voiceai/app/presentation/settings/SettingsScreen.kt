package com.voiceai.app.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showApiKey by remember { mutableStateOf(false) }
    var showSpeechApiKey by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear All Data") },
            text = { Text("This will permanently delete all your data including notes, scans, and settings. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearAllDialog = false
                    }
                ) {
                    Text("Delete Everything", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // ============================================================
            // Appearance
            // ============================================================
            item { SectionHeader("Appearance") }
            item {
                ListItem(
                    headlineContent = { Text("Theme") },
                    supportingContent = {
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = uiState.themeMode == mode,
                                    onClick = { viewModel.setTheme(mode) },
                                    label = {
                                        Text(
                                            mode.name.lowercase()
                                                .replaceFirstChar { it.uppercase() }
                                        )
                                    }
                                )
                            }
                        }
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Dynamic Colors") },
                    supportingContent = { Text("Use Material You dynamic color theming") },
                    trailingContent = {
                        Switch(
                            checked = uiState.dynamicColorEnabled,
                            onCheckedChange = { viewModel.setDynamicColor(it) }
                        )
                    }
                )
            }
            item {
                val languages = listOf(
                    "en" to "English",
                    "hi" to "Hindi",
                    "es" to "Spanish",
                    "fr" to "French",
                    "de" to "German"
                )
                var expanded by remember { mutableStateOf(false) }

                ListItem(
                    headlineContent = { Text("App UI Language") },
                    supportingContent = {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = languages.find { it.first == uiState.language }?.second
                                    ?: "English",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                languages.forEach { (code, name) ->
                                    DropdownMenuItem(
                                        text = { Text(name) },
                                        onClick = {
                                            viewModel.setLanguage(code)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }

            // ============================================================
            // Recording
            // ============================================================
            item { SectionHeader("Recording") }
            item {
                ListItem(
                    headlineContent = { Text("Audio Quality") },
                    supportingContent = {
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AudioQuality.entries.forEach { quality ->
                                FilterChip(
                                    selected = uiState.audioQuality == quality,
                                    onClick = { viewModel.setAudioQuality(quality) },
                                    label = {
                                        Text(
                                            quality.name.lowercase()
                                                .replaceFirstChar { it.uppercase() }
                                        )
                                    }
                                )
                            }
                        }
                    }
                )
            }
            item {
                val formats = listOf("m4a" to "M4A", "wav" to "WAV")
                ListItem(
                    headlineContent = { Text("Audio Format") },
                    supportingContent = {
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            formats.forEach { (value, label) ->
                                FilterChip(
                                    selected = uiState.audioFormat == value,
                                    onClick = { viewModel.setAudioFormat(value) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Noise Cancellation") },
                    supportingContent = { Text("Reduce background noise during recording") },
                    trailingContent = {
                        Switch(
                            checked = uiState.noiseCancellation,
                            onCheckedChange = { viewModel.setNoiseCancellation(it) }
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Auto-pause on Silence") },
                    supportingContent = { Text("Automatically pause when no speech is detected") },
                    trailingContent = {
                        Switch(
                            checked = uiState.autoPauseOnSilence,
                            onCheckedChange = { viewModel.setAutoPauseOnSilence(it) }
                        )
                    }
                )
            }
            item {
                val templates = listOf(
                    "general" to "General",
                    "meeting" to "Meeting",
                    "lecture" to "Lecture",
                    "interview" to "Interview",
                    "brainstorm" to "Brainstorm"
                )
                var expanded by remember { mutableStateOf(false) }

                ListItem(
                    headlineContent = { Text("Default Recording Template") },
                    supportingContent = {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = templates.find { it.first == uiState.defaultTemplate }?.second
                                    ?: "General",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                templates.forEach { (value, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            viewModel.setDefaultTemplate(value)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }

            // ============================================================
            // Scanner
            // ============================================================
            item { SectionHeader("Scanner") }
            item {
                ListItem(
                    headlineContent = { Text("Auto-capture") },
                    supportingContent = { Text("Automatically capture when document is detected") },
                    trailingContent = {
                        Switch(
                            checked = uiState.autoCaptureEnabled,
                            onCheckedChange = { viewModel.setAutoCapture(it) }
                        )
                    }
                )
            }
            item {
                val filterOptions = listOf("ORIGINAL", "GRAYSCALE", "BLACK_WHITE", "ENHANCED")
                var filterExpanded by remember { mutableStateOf(false) }

                ListItem(
                    headlineContent = { Text("Default Filter") },
                    supportingContent = {
                        ExposedDropdownMenuBox(
                            expanded = filterExpanded,
                            onExpandedChange = { filterExpanded = !filterExpanded },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.defaultScanFilter.lowercase()
                                    .replaceFirstChar { it.uppercase() }
                                    .replace("_", " "),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(filterExpanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = filterExpanded,
                                onDismissRequest = { filterExpanded = false }
                            ) {
                                filterOptions.forEach { filter ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                filter.lowercase()
                                                    .replaceFirstChar { it.uppercase() }
                                                    .replace("_", " ")
                                            )
                                        },
                                        onClick = {
                                            viewModel.setDefaultFilter(filter)
                                            filterExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
            item {
                val qualities = listOf("MEDIUM" to "Medium", "HIGH" to "High", "MAXIMUM" to "Maximum")
                ListItem(
                    headlineContent = { Text("Image Quality") },
                    supportingContent = {
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            qualities.forEach { (value, label) ->
                                FilterChip(
                                    selected = uiState.imageQuality == value,
                                    onClick = { viewModel.setImageQuality(value) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Auto-detect Document Type") },
                    supportingContent = { Text("Automatically classify documents (receipt, business card, etc.)") },
                    trailingContent = {
                        Switch(
                            checked = uiState.autoDetectDocType,
                            onCheckedChange = { viewModel.setAutoDetectDocType(it) }
                        )
                    }
                )
            }

            // ============================================================
            // Transcription & AI
            // ============================================================
            item { SectionHeader("Transcription & AI") }
            item {
                val allLanguages = listOf(
                    "en" to "English",
                    "hi" to "Hindi",
                    "es" to "Spanish",
                    "fr" to "French",
                    "de" to "German",
                    "zh" to "Chinese",
                    "ja" to "Japanese",
                    "ko" to "Korean",
                    "ar" to "Arabic",
                    "pt" to "Portuguese"
                )

                ListItem(
                    headlineContent = { Text("Transcription Languages") },
                    supportingContent = {
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            allLanguages.take(5).forEach { (code, name) ->
                                FilterChip(
                                    selected = uiState.transcriptionLanguages.contains(code),
                                    onClick = {
                                        val updated = if (uiState.transcriptionLanguages.contains(code)) {
                                            uiState.transcriptionLanguages - code
                                        } else {
                                            uiState.transcriptionLanguages + code
                                        }
                                        if (updated.isNotEmpty()) {
                                            viewModel.setTranscriptionLanguages(updated)
                                        }
                                    },
                                    label = { Text(name) }
                                )
                            }
                        }
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Speaker Detection") },
                    supportingContent = { Text("Identify different speakers in transcription") },
                    trailingContent = {
                        Switch(
                            checked = uiState.speakerDetection,
                            onCheckedChange = { viewModel.setSpeakerDetection(it) }
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Auto-extract Action Items") },
                    supportingContent = { Text("Automatically detect tasks and action items from notes") },
                    trailingContent = {
                        Switch(
                            checked = uiState.autoExtractActions,
                            onCheckedChange = { viewModel.setAutoExtractActions(it) }
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Auto-tag Notes") },
                    supportingContent = { Text("Automatically generate tags from note content") },
                    trailingContent = {
                        Switch(
                            checked = uiState.autoTagNotes,
                            onCheckedChange = { viewModel.setAutoTagNotes(it) }
                        )
                    }
                )
            }

            // ============================================================
            // Text-to-Speech
            // ============================================================
            item { SectionHeader("Text-to-Speech") }
            item {
                ListItem(
                    headlineContent = { Text("Speed") },
                    supportingContent = {
                        Column {
                            Text(
                                text = "${String.format("%.1f", uiState.ttsSpeed)}x",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = uiState.ttsSpeed,
                                onValueChange = { viewModel.setTtsSpeed(it) },
                                valueRange = 0.5f..2.0f,
                                steps = 5
                            )
                        }
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Pitch") },
                    supportingContent = {
                        Column {
                            Text(
                                text = "${String.format("%.1f", uiState.ttsPitch)}x",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = uiState.ttsPitch,
                                onValueChange = { viewModel.setTtsPitch(it) },
                                valueRange = 0.5f..2.0f,
                                steps = 5
                            )
                        }
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Voice Selection") },
                    supportingContent = { Text("Default system voice") }
                )
            }

            // ============================================================
            // Translation
            // ============================================================
            item { SectionHeader("Translation") }
            item {
                val translationLanguages = listOf(
                    "en" to "English",
                    "hi" to "Hindi",
                    "es" to "Spanish",
                    "fr" to "French",
                    "de" to "German",
                    "zh" to "Chinese",
                    "ja" to "Japanese",
                    "ar" to "Arabic"
                )
                var expanded by remember { mutableStateOf(false) }

                ListItem(
                    headlineContent = { Text("Default Target Language") },
                    supportingContent = {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = translationLanguages.find { it.first == uiState.defaultTranslationLang }?.second
                                    ?: "English",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                translationLanguages.forEach { (code, name) ->
                                    DropdownMenuItem(
                                        text = { Text(name) },
                                        onClick = {
                                            viewModel.setDefaultTranslationLang(code)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Auto-translate Scans") },
                    supportingContent = { Text("Automatically translate scanned document text") },
                    trailingContent = {
                        Switch(
                            checked = uiState.autoTranslateScans,
                            onCheckedChange = { viewModel.setAutoTranslateScans(it) }
                        )
                    }
                )
            }

            // ============================================================
            // Privacy & Security
            // ============================================================
            item { SectionHeader("Privacy & Security") }
            item {
                ListItem(
                    headlineContent = { Text("Biometric Lock") },
                    supportingContent = { Text("Require fingerprint or face to open app") },
                    trailingContent = {
                        Switch(
                            checked = uiState.biometricEnabled,
                            onCheckedChange = { viewModel.setBiometricEnabled(it) }
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Encrypt Storage") },
                    supportingContent = { Text("Encrypt all local data at rest") },
                    trailingContent = {
                        Switch(
                            checked = uiState.encryptStorage,
                            onCheckedChange = { viewModel.setEncryptStorage(it) }
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("On-device Only") },
                    supportingContent = { Text("Keep all processing on device, no cloud APIs") },
                    trailingContent = {
                        Switch(
                            checked = uiState.onDeviceOnly,
                            onCheckedChange = { viewModel.setOnDeviceOnly(it) }
                        )
                    }
                )
            }
            item {
                val deleteOptions = listOf(
                    0 to "Off",
                    7 to "7 days",
                    30 to "30 days",
                    90 to "90 days",
                    365 to "365 days"
                )
                var expanded by remember { mutableStateOf(false) }

                ListItem(
                    headlineContent = { Text("Auto-delete Timer") },
                    supportingContent = {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = deleteOptions.find { it.first == uiState.autoDeleteDays }?.second
                                    ?: "Off",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                deleteOptions.forEach { (days, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            viewModel.setAutoDeleteDays(days)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Clear All Data") },
                    supportingContent = { Text("Permanently delete all app data") },
                    leadingContent = {
                        Icon(
                            Icons.Filled.DeleteForever,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    trailingContent = {
                        OutlinedButton(onClick = { showClearAllDialog = true }) {
                            Text("Clear", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }

            // ============================================================
            // Cloud & Backup
            // ============================================================
            item { SectionHeader("Cloud & Backup") }
            item {
                ListItem(
                    headlineContent = { Text("Google Drive Sync") },
                    supportingContent = { Text("Sync data with Google Drive (coming soon)") },
                    trailingContent = {
                        Switch(
                            checked = uiState.cloudBackupEnabled,
                            onCheckedChange = { viewModel.setCloudBackupEnabled(it) }
                        )
                    }
                )
            }
            item {
                val frequencies = listOf(
                    "manual" to "Manual",
                    "daily" to "Daily",
                    "weekly" to "Weekly",
                    "monthly" to "Monthly"
                )
                var expanded by remember { mutableStateOf(false) }

                ListItem(
                    headlineContent = { Text("Backup Frequency") },
                    supportingContent = {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = frequencies.find { it.first == uiState.backupFrequency }?.second
                                    ?: "Manual",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                frequencies.forEach { (value, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            viewModel.setBackupFrequency(value)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Export All Data") },
                    supportingContent = { Text("Download everything as a ZIP file") },
                    trailingContent = {
                        FilledTonalButton(onClick = { viewModel.exportAllData() }) {
                            Text("Export ZIP")
                        }
                    }
                )
            }

            // ============================================================
            // Integrations
            // ============================================================
            item { SectionHeader("Integrations") }
            item {
                ListItem(
                    headlineContent = { Text("Calendar Sync") },
                    supportingContent = { Text("Sync action items with device calendar") },
                    trailingContent = {
                        Switch(
                            checked = uiState.calendarSync,
                            onCheckedChange = { viewModel.setCalendarSync(it) }
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Webhook URL") },
                    supportingContent = {
                        OutlinedTextField(
                            value = uiState.webhookUrl,
                            onValueChange = { viewModel.setWebhookUrl(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            singleLine = true,
                            placeholder = { Text("https://example.com/webhook") }
                        )
                    }
                )
            }

            // ============================================================
            // API Configuration
            // ============================================================
            item { SectionHeader("API Configuration") }
            item {
                ListItem(
                    headlineContent = { Text("Speech API Key") },
                    supportingContent = {
                        OutlinedTextField(
                            value = uiState.speechApiKey,
                            onValueChange = { viewModel.setSpeechApiKey(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            singleLine = true,
                            placeholder = { Text("Enter your Speech API key") },
                            visualTransformation = if (showSpeechApiKey) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(onClick = { showSpeechApiKey = !showSpeechApiKey }) {
                                    Icon(
                                        imageVector = if (showSpeechApiKey) {
                                            Icons.Filled.VisibilityOff
                                        } else {
                                            Icons.Filled.Visibility
                                        },
                                        contentDescription = "Toggle visibility"
                                    )
                                }
                            }
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Claude API Key") },
                    supportingContent = {
                        OutlinedTextField(
                            value = uiState.claudeApiKey,
                            onValueChange = { viewModel.setApiKey(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            singleLine = true,
                            placeholder = { Text("Enter your API key") },
                            visualTransformation = if (showApiKey) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(onClick = { showApiKey = !showApiKey }) {
                                    Icon(
                                        imageVector = if (showApiKey) {
                                            Icons.Filled.VisibilityOff
                                        } else {
                                            Icons.Filled.Visibility
                                        },
                                        contentDescription = "Toggle visibility"
                                    )
                                }
                            }
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("API Usage") },
                    supportingContent = { Text("Usage tracking coming soon") }
                )
            }

            // ============================================================
            // Storage
            // ============================================================
            item { SectionHeader("Storage") }
            item {
                ListItem(
                    headlineContent = { Text("Storage Used") },
                    supportingContent = { Text(uiState.storageUsed) }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Clear Cache") },
                    supportingContent = { Text("Remove temporary files") },
                    trailingContent = {
                        FilledTonalButton(onClick = { viewModel.clearCache() }) {
                            Text("Clear")
                        }
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Clear Old Recordings") },
                    supportingContent = { Text("Delete recordings older than auto-delete period") },
                    trailingContent = {
                        FilledTonalButton(onClick = { viewModel.clearOldRecordings() }) {
                            Text("Clear")
                        }
                    }
                )
            }

            // ============================================================
            // Notifications
            // ============================================================
            item { SectionHeader("Notifications") }
            item {
                val timeOptions = listOf(
                    "06:00" to "6:00 AM",
                    "07:00" to "7:00 AM",
                    "08:00" to "8:00 AM",
                    "09:00" to "9:00 AM",
                    "10:00" to "10:00 AM",
                    "12:00" to "12:00 PM",
                    "18:00" to "6:00 PM",
                    "20:00" to "8:00 PM"
                )
                var expanded by remember { mutableStateOf(false) }

                ListItem(
                    headlineContent = { Text("Daily Digest Time") },
                    supportingContent = {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = timeOptions.find { it.first == uiState.dailyDigestTime }?.second
                                    ?: uiState.dailyDigestTime,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                timeOptions.forEach { (value, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            viewModel.setDailyDigestTime(value)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Reminder Notifications") },
                    supportingContent = { Text("Get notified about upcoming action items") },
                    trailingContent = {
                        Switch(
                            checked = uiState.reminderNotifications,
                            onCheckedChange = { viewModel.setReminderNotifications(it) }
                        )
                    }
                )
            }

            // ============================================================
            // About
            // ============================================================
            item { SectionHeader("About") }
            item {
                ListItem(
                    headlineContent = { Text("App Version") },
                    supportingContent = { Text("2.0.0") },
                    leadingContent = {
                        Icon(Icons.Filled.Info, contentDescription = null)
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Rate on Play Store") },
                    leadingContent = {
                        Icon(Icons.Filled.Star, contentDescription = null)
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Share App") },
                    leadingContent = {
                        Icon(Icons.Filled.Share, contentDescription = null)
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Privacy Policy") },
                    leadingContent = {
                        Icon(Icons.Filled.Policy, contentDescription = null)
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Terms of Service") },
                    leadingContent = {
                        Icon(Icons.Filled.Description, contentDescription = null)
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Open Source Licenses") },
                    leadingContent = {
                        Icon(Icons.Filled.Code, contentDescription = null)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}
