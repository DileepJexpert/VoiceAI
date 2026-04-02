package com.voiceai.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_notes")
data class VoiceNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    @ColumnInfo(name = "audio_file_path")
    val audioFilePath: String,
    val transcript: String? = null,
    val summary: String? = null,
    val duration: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    val language: String = "en",
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    @ColumnInfo(name = "folder_id")
    val folderId: Long? = null,
    @ColumnInfo(name = "key_points")
    val keyPoints: String? = null,
    @ColumnInfo(name = "action_items")
    val actionItems: String? = null,
    val sentiment: String? = null,
    @ColumnInfo(name = "template_type")
    val templateType: String? = null,
    @ColumnInfo(name = "speaker_data")
    val speakerData: String? = null,
    val bookmarks: String? = null,
    @ColumnInfo(name = "linked_calendar_event_id")
    val linkedCalendarEventId: String? = null,
    @ColumnInfo(name = "auto_delete_at")
    val autoDeleteAt: Long? = null,
    @ColumnInfo(name = "is_encrypted")
    val isEncrypted: Boolean = false
)
