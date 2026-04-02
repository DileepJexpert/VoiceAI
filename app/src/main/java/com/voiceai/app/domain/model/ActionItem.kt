package com.voiceai.app.domain.model

data class ActionItem(
    val id: Long,
    val title: String,
    val sourceNoteId: Long?,
    val sourceScanId: Long?,
    val status: String,
    val priority: Int,
    val dueDate: Long?,
    val createdAt: Long,
    val completedAt: Long?
)
