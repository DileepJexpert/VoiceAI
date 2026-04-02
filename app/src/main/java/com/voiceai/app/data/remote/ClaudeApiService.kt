package com.voiceai.app.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ClaudeApiService {
    @POST("v1/messages")
    suspend fun createMessage(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body request: ClaudeRequest
    ): ClaudeResponse
}

data class ClaudeRequest(
    val model: String,
    val max_tokens: Int,
    val messages: List<ClaudeMessage>
)

data class ClaudeMessage(
    val role: String,
    val content: String
)

data class ClaudeResponse(
    val id: String,
    val content: List<ContentBlock>,
    val model: String,
    val stop_reason: String?
)

data class ContentBlock(
    val type: String,
    val text: String?
)
