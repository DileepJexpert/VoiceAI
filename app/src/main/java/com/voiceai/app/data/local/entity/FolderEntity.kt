package com.voiceai.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String,
    @ColumnInfo(name = "is_smart_folder")
    val isSmartFolder: Boolean = false,
    @ColumnInfo(name = "sort_rule")
    val sortRule: String? = null
)
