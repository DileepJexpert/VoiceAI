package com.voiceai.app.domain.usecase

import com.voiceai.app.domain.repository.TranslationRepository
import javax.inject.Inject

class TranslateTextUseCase @Inject constructor(
    private val translationRepository: TranslationRepository
) {
    suspend operator fun invoke(text: String, sourceLang: String, targetLang: String): String {
        return translationRepository.translate(text, sourceLang, targetLang)
    }
}
