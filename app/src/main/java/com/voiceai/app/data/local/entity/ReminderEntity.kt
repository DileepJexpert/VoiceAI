package com.voiceai.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    @ColumnInfo(name = "source_note_id")
    val sourceNoteId: Long? = null,
    @ColumnInfo(name = "source_scan_id")
    val sourceScanId: Long? = null,
    @ColumnInfo(name = "reminder_time")
    val reminderTime: Long,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,
    @ColumnInfo(name = "calendar_event_id")
    val calendarEventId: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
