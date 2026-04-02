package com.voiceai.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface TtsRepository {
    suspend fun speak(text: String)
    suspend fun pause()
    suspend fun resume()
    suspend fun stop()
    suspend fun setSpeechRate(rate: Float)
    fun isPlaying(): Flow<Boolean>
}
