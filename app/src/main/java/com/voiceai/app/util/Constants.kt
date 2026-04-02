package com.voiceai.app.util

object Constants {
    const val DATABASE_NAME = "voiceai_database"
    const val AUDIO_DIR = "audio"
    const val SCANS_DIR = "scans"
    const val PDFS_DIR = "pdfs"
    const val DEFAULT_LANGUAGE = "en"
    const val AUDIO_SAMPLE_RATE = 44100
    const val AUDIO_FORMAT = ".m4a"
    const val IMAGE_FORMAT = ".jpg"
    const val PDF_FORMAT = ".pdf"
    const val CLAUDE_API_BASE_URL = "https://api.anthropic.com/"
    const val CLAUDE_MODEL = "claude-sonnet-4-20250514"
    const val AUTO_CAPTURE_DELAY_MS = 1500L
    const val WAVEFORM_SPIKE_COUNT = 50
    const val MAX_RECORDING_DURATION_MS = 3600000L // 1 hour
    const val TTS_DEFAULT_SPEED = 1.0f
    const val TTS_MIN_SPEED = 0.5f
    const val TTS_MAX_SPEED = 2.0f
}
