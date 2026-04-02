package com.voiceai.app.domain.repository

interface SpeechToTextRepository {
    suspend fun transcribe(audioFilePath: String, language: String): String
}
