package com.voiceai.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.voiceai.app.data.remote.ClaudeApiService
import com.voiceai.app.data.remote.ClaudeMessage
import com.voiceai.app.data.remote.ClaudeRequest
import com.voiceai.app.domain.repository.AISummaryRepository
import com.voiceai.app.util.Constants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AISummaryRepositoryImpl @Inject constructor(
    private val claudeApiService: ClaudeApiService,
    private val dataStore: DataStore<Preferences>
) : AISummaryRepository {

    companion object {
        private val API_KEY = stringPreferencesKey("claude_api_key")

        private const val VOICE_NOTE_PROMPT =
            "Summarize this voice note concisely. Extract: " +
            "1) A concise summary (2-3 sentences), " +
            "2) Key points as bullet list, " +
            "3) Any action items. " +
            "Return as JSON with keys: summary, keyPoints, actionItems"

        private const val DOCUMENT_PROMPT =
            "Analyze this scanned document text. Identify: " +
            "1) Document type, " +
            "2) A concise summary, " +
            "3) Key information, " +
            "4) Any action items. " +
            "Return as JSON with keys: documentType, summary, keyInfo, actionItems"
    }

    private suspend fun getApiKey(): String {
        return dataStore.data.map { preferences ->
            preferences[API_KEY] ?: throw IllegalStateException("Claude API key not configured")
        }.first()
    }

    override suspend fun summarizeVoiceNote(transcript: String): String {
        val apiKey = getApiKey()
        val request = ClaudeRequest(
            model = Constants.CLAUDE_MODEL,
            max_tokens = 1024,
            messages = listOf(
                ClaudeMessage(
                    role = "user",
                    content = "$VOICE_NOTE_PROMPT\n\nTranscript:\n$transcript"
                )
            )
        )

        val response = claudeApiService.createMessage(apiKey = apiKey, request = request)
        return response.content
            .firstOrNull { it.type == "text" }
            ?.text
            ?: throw RuntimeException("No text content in Claude response")
    }

    override suspend fun summarizeDocument(text: String): String {
        val apiKey = getApiKey()
        val request = ClaudeRequest(
            model = Constants.CLAUDE_MODEL,
            max_tokens = 1024,
            messages = listOf(
                ClaudeMessage(
                    role = "user",
                    content = "$DOCUMENT_PROMPT\n\nDocument text:\n$text"
                )
            )
        )

        val response = claudeApiService.createMessage(apiKey = apiKey, request = request)
        return response.content
            .firstOrNull { it.type == "text" }
            ?.text
            ?: throw RuntimeException("No text content in Claude response")
    }
}
