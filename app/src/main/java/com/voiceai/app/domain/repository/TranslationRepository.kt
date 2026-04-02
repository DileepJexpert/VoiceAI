package com.voiceai.app.domain.repository

interface TranslationRepository {
    suspend fun translate(text: String, sourceLang: String, targetLang: String): String
}
