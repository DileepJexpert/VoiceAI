package com.voiceai.app.domain.usecase

import com.voiceai.app.domain.repository.AISummaryRepository
import com.voiceai.app.domain.repository.OcrRepository
import javax.inject.Inject

class ScanBusinessCardUseCase @Inject constructor(
    private val ocrRepository: OcrRepository,
    private val aiSummaryRepository: AISummaryRepository
) {
    suspend operator fun invoke(imagePath: String): String {
        val extractedText = ocrRepository.extractText(imagePath)
        return aiSummaryRepository.summarizeDocument(extractedText)
    }
}
