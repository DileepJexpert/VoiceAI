package com.voiceai.app.domain.usecase

import com.voiceai.app.domain.repository.TtsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TextToSpeechUseCase @Inject constructor(
    private val ttsRepository: TtsRepository
) {
    suspend fun speak(text: String) {
        ttsRepository.speak(text)
    }

    suspend fun pause() {
        ttsRepository.pause()
    }

    suspend fun resume() {
        ttsRepository.resume()
    }

    suspend fun stop() {
        ttsRepository.stop()
    }

    suspend fun setSpeechRate(rate: Float) {
        ttsRepository.setSpeechRate(rate)
    }

    fun isPlaying(): Flow<Boolean> {
        return ttsRepository.isPlaying()
    }
}
