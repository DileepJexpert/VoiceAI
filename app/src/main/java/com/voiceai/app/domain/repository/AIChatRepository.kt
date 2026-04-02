package com.voiceai.app.domain.repository

import kotlinx.coroutines.flow.Flow

data class ChatMessage(
    val role: String,
    val content: String,
    val timestamp: Long,
    val sourceReferences: List<String> = emptyList()
)

interface AIChatRepository {
    fun sendMessage(message: String, context: List<String>): Flow<String>
    suspend fun getConversationHistory(): List<ChatMessage>
}
