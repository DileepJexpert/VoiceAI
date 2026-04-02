package com.voiceai.app.domain.usecase

import com.voiceai.app.domain.repository.AISummaryRepository
import javax.inject.Inject

class SummarizeNoteUseCase @Inject constructor(
    private val aiSummaryRepository: AISummaryRepository
) {
    suspend operator fun invoke(transcript: String): String {
        return aiSummaryRepository.summarizeVoiceNote(transcript)
    }
}
