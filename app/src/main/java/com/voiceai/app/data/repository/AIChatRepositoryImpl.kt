package com.voiceai.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.voiceai.app.data.remote.ClaudeApiService
import com.voiceai.app.data.remote.ClaudeMessage
import com.voiceai.app.data.remote.ClaudeRequest
import com.voiceai.app.domain.repository.AIChatRepository
import com.voiceai.app.domain.repository.ChatMessage
import com.voiceai.app.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIChatRepositoryImpl @Inject constructor(
    private val claudeApiService: ClaudeApiService,
    private val dataStore: DataStore<Preferences>
) : AIChatRepository {

    companion object {
        private val API_KEY = stringPreferencesKey("claude_api_key")
        private const val SYSTEM_CONTEXT_PREFIX =
            "You are an AI assistant for VoiceAI, a voice notes and document scanning app. " +
            "Answer questions based on the user's notes and documents when context is provided. " +
            "When referencing specific sources, include them in square brackets like [Source: note title]."
    }

    private val conversationHistory = mutableListOf<ChatMessage>()

    private suspend fun getApiKey(): String {
        return dataStore.data.map { preferences ->
            preferences[API_KEY] ?: throw IllegalStateException("Claude API key not configured")
        }.first()
    }

    override fun sendMessage(message: String, context: List<String>): Flow<String> = flow {
        val apiKey = getApiKey()

        // Build the user message with context
        val userContent = if (context.isNotEmpty()) {
            val contextBlock = context.joinToString("\n\n") { "---\n$it\n---" }
            "Context from user's notes and documents:\n$contextBlock\n\nUser question: $message"
        } else {
            message
        }

        // Build messages list from conversation history
        val messages = mutableListOf<ClaudeMessage>()

        // Add system context as first user message if this is the start
        if (conversationHistory.isEmpty()) {
            messages.add(ClaudeMessage(role = "user", content = "$SYSTEM_CONTEXT_PREFIX\n\n$userContent"))
        } else {
            // Add previous conversation turns
            for (chatMessage in conversationHistory) {
                messages.add(ClaudeMessage(role = chatMessage.role, content = chatMessage.content))
            }
            messages.add(ClaudeMessage(role = "user", content = userContent))
        }

        val request = ClaudeRequest(
            model = Constants.CLAUDE_MODEL,
            max_tokens = 2048,
            messages = messages
        )

        val response = claudeApiService.createMessage(apiKey = apiKey, request = request)
        val responseText = response.content
            .firstOrNull { it.type == "text" }
            ?.text
            ?: throw RuntimeException("No text content in Claude response")

        // Parse source references from response
        val sourceReferences = parseSourceReferences(responseText)

        // Add to conversation history
        conversationHistory.add(
            ChatMessage(
                role = "user",
                content = userContent,
                timestamp = System.currentTimeMillis()
            )
        )
        conversationHistory.add(
            ChatMessage(
                role = "assistant",
                content = responseText,
                timestamp = System.currentTimeMillis(),
                sourceReferences = sourceReferences
            )
        )

        emit(responseText)
    }

    override suspend fun getConversationHistory(): List<ChatMessage> {
        return conversationHistory.toList()
    }

    private fun parseSourceReferences(text: String): List<String> {
        val regex = Regex("\\[Source:\\s*(.+?)\\]")
        return regex.findAll(text).map { it.groupValues[1].trim() }.toList()
    }
}
