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
    val tags: List<Tag> = emptyList()
)
