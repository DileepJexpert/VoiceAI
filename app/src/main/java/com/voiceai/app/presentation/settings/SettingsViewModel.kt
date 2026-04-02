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
    val dynamicColorEnabled: Boolean = true,
    val language: String = "en",
    val audioQuality: AudioQuality = AudioQuality.MEDIUM,
    val audioFormat: String = "m4a",
    val noiseCancellation: Boolean = false,
    val autoPauseOnSilence: Boolean = false,
    val defaultTemplate: String = "general",
    val autoCaptureEnabled: Boolean = true,
    val defaultScanFilter: String = "ORIGINAL",
    val imageQuality: String = "HIGH",
    val autoDetectDocType: Boolean = true,
    val transcriptionLanguages: List<String> = listOf("en"),
    val speakerDetection: Boolean = true,
    val autoExtractActions: Boolean = true,
    val autoTagNotes: Boolean = true,
    val ttsSpeed: Float = 1.0f,
    val ttsPitch: Float = 1.0f,
    val defaultTranslationLang: String = "en",
    val autoTranslateScans: Boolean = false,
    val biometricEnabled: Boolean = false,
    val encryptStorage: Boolean = false,
    val onDeviceOnly: Boolean = false,
    val autoDeleteDays: Int = 0, // 0 = off
    val cloudBackupEnabled: Boolean = false,
    val backupFrequency: String = "manual",
    val calendarSync: Boolean = false,
    val webhookUrl: String = "",
    val dailyDigestTime: String = "08:00",
    val reminderNotifications: Boolean = true,
    val speechApiKey: String = "",
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
                        dynamicColorEnabled = prefs[DYNAMIC_COLOR_KEY] ?: true,
                        language = prefs[LANGUAGE_KEY] ?: "en",
                        audioQuality = AudioQuality.valueOf(prefs[AUDIO_QUALITY_KEY] ?: AudioQuality.MEDIUM.name),
                        audioFormat = prefs[AUDIO_FORMAT_KEY] ?: "m4a",
                        noiseCancellation = prefs[NOISE_CANCELLATION_KEY] ?: false,
                        autoPauseOnSilence = prefs[AUTO_PAUSE_SILENCE_KEY] ?: false,
                        defaultTemplate = prefs[DEFAULT_TEMPLATE_KEY] ?: "general",
                        autoCaptureEnabled = prefs[AUTO_CAPTURE_KEY] ?: true,
                        defaultScanFilter = prefs[DEFAULT_FILTER_KEY] ?: "ORIGINAL",
                        imageQuality = prefs[IMAGE_QUALITY_KEY] ?: "HIGH",
                        autoDetectDocType = prefs[AUTO_DETECT_DOC_TYPE_KEY] ?: true,
                        transcriptionLanguages = (prefs[TRANSCRIPTION_LANGUAGES_KEY] ?: "en").split(","),
                        speakerDetection = prefs[SPEAKER_DETECTION_KEY] ?: true,
                        autoExtractActions = prefs[AUTO_EXTRACT_ACTIONS_KEY] ?: true,
                        autoTagNotes = prefs[AUTO_TAG_NOTES_KEY] ?: true,
                        ttsSpeed = prefs[TTS_SPEED_KEY] ?: 1.0f,
                        ttsPitch = prefs[TTS_PITCH_KEY] ?: 1.0f,
                        defaultTranslationLang = prefs[DEFAULT_TRANSLATION_LANG_KEY] ?: "en",
                        autoTranslateScans = prefs[AUTO_TRANSLATE_SCANS_KEY] ?: false,
                        biometricEnabled = prefs[BIOMETRIC_ENABLED_KEY] ?: false,
                        encryptStorage = prefs[ENCRYPT_STORAGE_KEY] ?: false,
                        onDeviceOnly = prefs[ON_DEVICE_ONLY_KEY] ?: false,
                        autoDeleteDays = prefs[AUTO_DELETE_DAYS_KEY] ?: 0,
                        cloudBackupEnabled = prefs[CLOUD_BACKUP_ENABLED_KEY] ?: false,
                        backupFrequency = prefs[BACKUP_FREQUENCY_KEY] ?: "manual",
                        calendarSync = prefs[CALENDAR_SYNC_KEY] ?: false,
                        webhookUrl = prefs[WEBHOOK_URL_KEY] ?: "",
                        dailyDigestTime = prefs[DAILY_DIGEST_TIME_KEY] ?: "08:00",
                        reminderNotifications = prefs[REMINDER_NOTIFICATIONS_KEY] ?: true,
                        speechApiKey = prefs[SPEECH_API_KEY] ?: "",
                        claudeApiKey = prefs[API_KEY] ?: ""
                    )
                }
            }
        }
    }

    fun setTheme(mode: ThemeMode) = save(THEME_KEY, mode.name)
    fun setDynamicColor(enabled: Boolean) = save(DYNAMIC_COLOR_KEY, enabled)
    fun setLanguage(lang: String) = save(LANGUAGE_KEY, lang)
    fun setAudioQuality(quality: AudioQuality) = save(AUDIO_QUALITY_KEY, quality.name)
    fun setAudioFormat(format: String) = save(AUDIO_FORMAT_KEY, format)
    fun setNoiseCancellation(enabled: Boolean) = save(NOISE_CANCELLATION_KEY, enabled)
    fun setAutoPauseOnSilence(enabled: Boolean) = save(AUTO_PAUSE_SILENCE_KEY, enabled)
    fun setDefaultTemplate(template: String) = save(DEFAULT_TEMPLATE_KEY, template)
    fun setAutoCapture(enabled: Boolean) = save(AUTO_CAPTURE_KEY, enabled)
    fun setDefaultFilter(filter: String) = save(DEFAULT_FILTER_KEY, filter)
    fun setImageQuality(quality: String) = save(IMAGE_QUALITY_KEY, quality)
    fun setAutoDetectDocType(enabled: Boolean) = save(AUTO_DETECT_DOC_TYPE_KEY, enabled)
    fun setTranscriptionLanguages(languages: List<String>) = save(TRANSCRIPTION_LANGUAGES_KEY, languages.joinToString(","))
    fun setSpeakerDetection(enabled: Boolean) = save(SPEAKER_DETECTION_KEY, enabled)
    fun setAutoExtractActions(enabled: Boolean) = save(AUTO_EXTRACT_ACTIONS_KEY, enabled)
    fun setAutoTagNotes(enabled: Boolean) = save(AUTO_TAG_NOTES_KEY, enabled)
    fun setTtsSpeed(speed: Float) = save(TTS_SPEED_KEY, speed)
    fun setTtsPitch(pitch: Float) = save(TTS_PITCH_KEY, pitch)
    fun setDefaultTranslationLang(lang: String) = save(DEFAULT_TRANSLATION_LANG_KEY, lang)
    fun setAutoTranslateScans(enabled: Boolean) = save(AUTO_TRANSLATE_SCANS_KEY, enabled)
    fun setBiometricEnabled(enabled: Boolean) = save(BIOMETRIC_ENABLED_KEY, enabled)
    fun setEncryptStorage(enabled: Boolean) = save(ENCRYPT_STORAGE_KEY, enabled)
    fun setOnDeviceOnly(enabled: Boolean) = save(ON_DEVICE_ONLY_KEY, enabled)
    fun setAutoDeleteDays(days: Int) = save(AUTO_DELETE_DAYS_KEY, days)
    fun setCloudBackupEnabled(enabled: Boolean) = save(CLOUD_BACKUP_ENABLED_KEY, enabled)
    fun setBackupFrequency(frequency: String) = save(BACKUP_FREQUENCY_KEY, frequency)
    fun setCalendarSync(enabled: Boolean) = save(CALENDAR_SYNC_KEY, enabled)
    fun setWebhookUrl(url: String) = save(WEBHOOK_URL_KEY, url)
    fun setDailyDigestTime(time: String) = save(DAILY_DIGEST_TIME_KEY, time)
    fun setReminderNotifications(enabled: Boolean) = save(REMINDER_NOTIFICATIONS_KEY, enabled)
    fun setSpeechApiKey(key: String) = save(SPEECH_API_KEY, key)
    fun setApiKey(key: String) = save(API_KEY, key)

    fun clearCache() {
        // Placeholder for cache clearing
    }

    fun clearOldRecordings() {
        // Placeholder for clearing old recordings
    }

    fun clearAllData() {
        viewModelScope.launch {
            dataStore.edit { it.clear() }
            _uiState.value = SettingsUiState()
        }
    }

    fun exportAllData() {
        // Placeholder for ZIP export
    }

    private fun <T> save(key: Preferences.Key<T>, value: T) {
        viewModelScope.launch {
            dataStore.edit { it[key] = value }
        }
    }

    companion object {
        val THEME_KEY = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val AUDIO_QUALITY_KEY = stringPreferencesKey("audio_quality")
        val AUDIO_FORMAT_KEY = stringPreferencesKey("audio_format")
        val NOISE_CANCELLATION_KEY = booleanPreferencesKey("noise_cancellation")
        val AUTO_PAUSE_SILENCE_KEY = booleanPreferencesKey("auto_pause_on_silence")
        val DEFAULT_TEMPLATE_KEY = stringPreferencesKey("default_template")
        val AUTO_CAPTURE_KEY = booleanPreferencesKey("auto_capture")
        val DEFAULT_FILTER_KEY = stringPreferencesKey("default_scan_filter")
        val IMAGE_QUALITY_KEY = stringPreferencesKey("image_quality")
        val AUTO_DETECT_DOC_TYPE_KEY = booleanPreferencesKey("auto_detect_doc_type")
        val TRANSCRIPTION_LANGUAGES_KEY = stringPreferencesKey("transcription_languages")
        val SPEAKER_DETECTION_KEY = booleanPreferencesKey("speaker_detection")
        val AUTO_EXTRACT_ACTIONS_KEY = booleanPreferencesKey("auto_extract_actions")
        val AUTO_TAG_NOTES_KEY = booleanPreferencesKey("auto_tag_notes")
        val TTS_SPEED_KEY = floatPreferencesKey("tts_speed")
        val TTS_PITCH_KEY = floatPreferencesKey("tts_pitch")
        val DEFAULT_TRANSLATION_LANG_KEY = stringPreferencesKey("default_translation_lang")
        val AUTO_TRANSLATE_SCANS_KEY = booleanPreferencesKey("auto_translate_scans")
        val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")
        val ENCRYPT_STORAGE_KEY = booleanPreferencesKey("encrypt_storage")
        val ON_DEVICE_ONLY_KEY = booleanPreferencesKey("on_device_only")
        val AUTO_DELETE_DAYS_KEY = intPreferencesKey("auto_delete_days")
        val CLOUD_BACKUP_ENABLED_KEY = booleanPreferencesKey("cloud_backup_enabled")
        val BACKUP_FREQUENCY_KEY = stringPreferencesKey("backup_frequency")
        val CALENDAR_SYNC_KEY = booleanPreferencesKey("calendar_sync")
        val WEBHOOK_URL_KEY = stringPreferencesKey("webhook_url")
        val DAILY_DIGEST_TIME_KEY = stringPreferencesKey("daily_digest_time")
        val REMINDER_NOTIFICATIONS_KEY = booleanPreferencesKey("reminder_notifications")
        val SPEECH_API_KEY = stringPreferencesKey("speech_api_key")
        val API_KEY = stringPreferencesKey("claude_api_key")
    }
}
