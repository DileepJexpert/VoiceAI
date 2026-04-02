package com.voiceai.app.domain.usecase

import com.voiceai.app.domain.repository.SpeechToTextRepository
import javax.inject.Inject

class TranscribeAudioUseCase @Inject constructor(
    private val speechToTextRepository: SpeechToTextRepository
) {
    suspend operator fun invoke(filePath: String, language: String): String {
        return speechToTextRepository.transcribe(filePath, language)
    }
}
