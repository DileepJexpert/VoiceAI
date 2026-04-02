package com.voiceai.app.domain.model

data class Reminder(
    val id: Long,
    val title: String,
    val description: String?,
    val sourceNoteId: Long?,
    val sourceScanId: Long?,
    val reminderTime: Long,
    val isCompleted: Boolean,
    val calendarEventId: String?,
    val createdAt: Long
)
