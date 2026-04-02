package com.voiceai.app.domain.model

data class ScannedDocument(
    val id: Long,
    val title: String,
    val extractedText: String?,
    val summary: String?,
    val pageCount: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val language: String,
    val isFavorite: Boolean,
    val folderId: Long?,
    val pdfFilePath: String?,
    val pages: List<ScannedPage> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val translatedText: String? = null,
    val keyInfo: String? = null,
    val documentType: String? = null,
    val detectedLanguage: String? = null,
    val autoDeleteAt: Long? = null,
    val isEncrypted: Boolean = false
)
