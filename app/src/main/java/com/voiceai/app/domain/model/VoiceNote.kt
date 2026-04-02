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
    val tags: List<Tag> = emptyList(),
    val keyPoints: List<String> = emptyList(),
    val actionItems: List<String> = emptyList(),
    val sentiment: String? = null,
    val templateType: String? = null,
    val speakerData: String? = null,
    val bookmarks: List<Long> = emptyList(),
    val linkedCalendarEventId: String? = null,
    val autoDeleteAt: Long? = null,
    val isEncrypted: Boolean = false
)
