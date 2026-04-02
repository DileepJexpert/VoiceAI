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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
            // --- Appearance Section ---
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

            // --- Language Section ---
            item { SectionHeader("Language") }
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
                    headlineContent = { Text("Language") },
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

            // --- Recording Section ---
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

            // --- Scanner Section ---
            item { SectionHeader("Scanner") }
            item {
                ListItem(
                    headlineContent = { Text("Auto-capture") },
                    supportingContent = {
                        Text("Automatically capture when document is detected")
                    },
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

            // --- Text-to-Speech Section ---
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

            // --- API Configuration Section ---
            item { SectionHeader("API Configuration") }
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

            // --- Storage Section ---
            item { SectionHeader("Storage") }
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

            // --- About Section ---
            item { SectionHeader("About") }
            item {
                ListItem(
                    headlineContent = { Text("App Version") },
                    supportingContent = { Text("1.0.0") },
                    leadingContent = {
                        Icon(Icons.Filled.Info, contentDescription = null)
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Rate App") },
                    leadingContent = {
                        Icon(Icons.Filled.Star, contentDescription = null)
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
