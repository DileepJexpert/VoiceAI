package com.voiceai.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_documents")
data class ScannedDocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    @ColumnInfo(name = "extracted_text")
    val extractedText: String? = null,
    val summary: String? = null,
    @ColumnInfo(name = "page_count")
    val pageCount: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    val language: String = "en",
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    @ColumnInfo(name = "folder_id")
    val folderId: Long? = null,
    @ColumnInfo(name = "pdf_file_path")
    val pdfFilePath: String? = null
)
