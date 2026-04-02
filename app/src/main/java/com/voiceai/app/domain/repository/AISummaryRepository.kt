package com.voiceai.app.domain.repository

interface AISummaryRepository {
    suspend fun summarizeVoiceNote(transcript: String): String
    suspend fun summarizeDocument(text: String): String
}
