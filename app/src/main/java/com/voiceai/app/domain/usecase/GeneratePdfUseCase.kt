package com.voiceai.app.domain.usecase

import com.voiceai.app.domain.model.ScannedPage
import com.voiceai.app.domain.repository.PdfRepository
import javax.inject.Inject

class GeneratePdfUseCase @Inject constructor(
    private val pdfRepository: PdfRepository
) {
    suspend operator fun invoke(pages: List<ScannedPage>, title: String): String {
        return pdfRepository.generatePdf(pages, title)
    }
}
