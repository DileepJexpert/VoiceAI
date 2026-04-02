package com.voiceai.app.domain.model

data class VoiceNote(
    val id: Long,
    val title: String,
    val audioFilePath: String,
    val transcript: String?,
    val summary: String?,
    val duration: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val language: String,
    val isFavorite: Boolean,
    val folderId: Long?,
    val tags: List<Tag> = emptyList()
)
