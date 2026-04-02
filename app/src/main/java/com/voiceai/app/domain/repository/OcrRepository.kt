package com.voiceai.app.domain.repository

interface OcrRepository {
    suspend fun extractText(imagePath: String): String
}
