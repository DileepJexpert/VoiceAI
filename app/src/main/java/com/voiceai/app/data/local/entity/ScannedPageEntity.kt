package com.voiceai.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scanned_pages",
    foreignKeys = [
        ForeignKey(
            entity = ScannedDocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["document_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["document_id"])]
)
data class ScannedPageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "document_id")
    val documentId: Long,
    @ColumnInfo(name = "page_number")
    val pageNumber: Int,
    @ColumnInfo(name = "image_path")
    val imagePath: String,
    @ColumnInfo(name = "raw_image_path")
    val rawImagePath: String,
    @ColumnInfo(name = "page_text")
    val pageText: String? = null,
    val filter: String = "ORIGINAL",
    @ColumnInfo(name = "table_data")
    val tableData: String? = null
)
