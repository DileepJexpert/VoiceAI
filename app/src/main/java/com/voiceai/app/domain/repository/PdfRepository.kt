package com.voiceai.app.domain.repository

import com.voiceai.app.domain.model.ScannedPage

interface PdfRepository {
    suspend fun generatePdf(pages: List<ScannedPage>, title: String): String
}
