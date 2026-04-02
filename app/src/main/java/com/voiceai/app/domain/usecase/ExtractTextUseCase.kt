package com.voiceai.app.domain.usecase

import com.voiceai.app.domain.repository.OcrRepository
import javax.inject.Inject

class ExtractTextUseCase @Inject constructor(
    private val ocrRepository: OcrRepository
) {
    suspend operator fun invoke(imagePath: String): String {
        return ocrRepository.extractText(imagePath)
    }
}
