package com.voiceai.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.voiceai.app.data.remote.ClaudeApiService
import com.voiceai.app.data.remote.ClaudeMessage
import com.voiceai.app.data.remote.ClaudeRequest
import com.voiceai.app.domain.repository.TranslationRepository
import com.voiceai.app.util.Constants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationRepositoryImpl @Inject constructor(
    private val claudeApiService: ClaudeApiService,
    private val dataStore: DataStore<Preferences>
) : TranslationRepository {

    companion object {
        private val API_KEY = stringPreferencesKey("claude_api_key")
    }

    private suspend fun getApiKey(): String {
        return dataStore.data.map { preferences ->
            preferences[API_KEY] ?: throw IllegalStateException("Claude API key not configured")
        }.first()
    }

    override suspend fun translate(text: String, sourceLang: String, targetLang: String): String {
        val apiKey = getApiKey()
        val prompt = "Translate the following text from $sourceLang to $targetLang. " +
                "Return only the translated text without any explanation or additional commentary.\n\n" +
                "Text to translate:\n$text"

        val request = ClaudeRequest(
            model = Constants.CLAUDE_MODEL,
            max_tokens = 2048,
            messages = listOf(
                ClaudeMessage(
                    role = "user",
                    content = prompt
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
