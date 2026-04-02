package com.voiceai.app.presentation.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ThemeMode { LIGHT, DARK, SYSTEM }
enum class AudioQuality { LOW, MEDIUM, HIGH }

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: String = "en",
    val audioQuality: AudioQuality = AudioQuality.MEDIUM,
    val autoCaptureEnabled: Boolean = true,
    val defaultScanFilter: String = "ORIGINAL",
    val ttsSpeed: Float = 1.0f,
    val ttsPitch: Float = 1.0f,
    val claudeApiKey: String = "",
    val storageUsed: String = "0 MB"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                _uiState.update {
                    it.copy(
                        themeMode = ThemeMode.valueOf(prefs[THEME_KEY] ?: ThemeMode.SYSTEM.name),
                        language = prefs[LANGUAGE_KEY] ?: "en",
                        audioQuality = AudioQuality.valueOf(prefs[AUDIO_QUALITY_KEY] ?: AudioQuality.MEDIUM.name),
                        autoCaptureEnabled = prefs[AUTO_CAPTURE_KEY] ?: true,
                        defaultScanFilter = prefs[DEFAULT_FILTER_KEY] ?: "ORIGINAL",
                        ttsSpeed = prefs[TTS_SPEED_KEY] ?: 1.0f,
                        ttsPitch = prefs[TTS_PITCH_KEY] ?: 1.0f,
                        claudeApiKey = prefs[API_KEY] ?: ""
                    )
                }
            }
        }
    }

    fun setTheme(mode: ThemeMode) = save(THEME_KEY, mode.name)
    fun setLanguage(lang: String) = save(LANGUAGE_KEY, lang)
    fun setAudioQuality(quality: AudioQuality) = save(AUDIO_QUALITY_KEY, quality.name)
    fun setAutoCapture(enabled: Boolean) = save(AUTO_CAPTURE_KEY, enabled)
    fun setDefaultFilter(filter: String) = save(DEFAULT_FILTER_KEY, filter)
    fun setTtsSpeed(speed: Float) = save(TTS_SPEED_KEY, speed)
    fun setTtsPitch(pitch: Float) = save(TTS_PITCH_KEY, pitch)
    fun setApiKey(key: String) = save(API_KEY, key)

    fun clearCache() {
        // Placeholder for cache clearing
    }

    private fun <T> save(key: Preferences.Key<T>, value: T) {
        viewModelScope.launch {
            dataStore.edit { it[key] = value }
        }
    }

    companion object {
        val THEME_KEY = stringPreferencesKey("theme_mode")
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val AUDIO_QUALITY_KEY = stringPreferencesKey("audio_quality")
        val AUTO_CAPTURE_KEY = booleanPreferencesKey("auto_capture")
        val DEFAULT_FILTER_KEY = stringPreferencesKey("default_scan_filter")
        val TTS_SPEED_KEY = floatPreferencesKey("tts_speed")
        val TTS_PITCH_KEY = floatPreferencesKey("tts_pitch")
        val API_KEY = stringPreferencesKey("claude_api_key")
    }
}
