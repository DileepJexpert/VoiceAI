package com.voiceai.app.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Appearance Section
            item {
                SectionHeader("Appearance")
            }
            item {
                Text(
                    "Theme",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = uiState.themeMode == mode,
                            onClick = { viewModel.setTheme(mode) },
                            label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }

            // Language Section
            item {
                SectionHeader("Language")
            }
            item {
                val languages = listOf("en" to "English", "hi" to "Hindi", "es" to "Spanish", "fr" to "French", "de" to "German")
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = languages.find { it.first == uiState.language }?.second ?: "English",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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

            // Recording Section
            item {
                SectionHeader("Recording")
            }
            item {
                Text(
                    "Audio Quality",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AudioQuality.entries.forEach { quality ->
                        FilterChip(
                            selected = uiState.audioQuality == quality,
                            onClick = { viewModel.setAudioQuality(quality) },
                            label = { Text(quality.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }

            // Scanner Section
            item {
                SectionHeader("Scanner")
            }
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

            // Text-to-Speech Section
            item {
                SectionHeader("Text-to-Speech")
            }
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Speed: ${String.format("%.1f", uiState.ttsSpeed)}x")
                    Slider(
                        value = uiState.ttsSpeed,
                        onValueChange = { viewModel.setTtsSpeed(it) },
                        valueRange = 0.5f..2.0f,
                        steps = 5
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Pitch: ${String.format("%.1f", uiState.ttsPitch)}x")
                    Slider(
                        value = uiState.ttsPitch,
                        onValueChange = { viewModel.setTtsPitch(it) },
                        valueRange = 0.5f..2.0f,
                        steps = 5
                    )
                }
            }

            // API Configuration Section
            item {
                SectionHeader("API Configuration")
            }
            item {
                OutlinedTextField(
                    value = uiState.claudeApiKey,
                    onValueChange = { viewModel.setApiKey(it) },
                    label = { Text("Claude API Key") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true,
                    visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showApiKey = !showApiKey }) {
                            Icon(
                                if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                "Toggle visibility"
                            )
                        }
                    }
                )
            }

            // Storage Section
            item {
                SectionHeader("Storage")
            }
            item {
                ListItem(
                    headlineContent = { Text("Storage Used") },
                    supportingContent = { Text(uiState.storageUsed) },
                    trailingContent = {
                        FilledTonalButton(onClick = { viewModel.clearCache() }) {
                            Text("Clear Cache")
                        }
                    }
                )
            }

            // About Section
            item {
                SectionHeader("About")
            }
            item {
                ListItem(
                    headlineContent = { Text("VoiceAI") },
                    supportingContent = { Text("Version 1.0.0") },
                    leadingContent = {
                        Icon(Icons.Default.Info, contentDescription = null)
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
