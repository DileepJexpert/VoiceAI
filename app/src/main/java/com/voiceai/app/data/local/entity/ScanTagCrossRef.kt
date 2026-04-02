package com.voiceai.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "scan_tag_cross_ref",
    primaryKeys = ["scan_id", "tag_id"],
    indices = [
        Index(value = ["scan_id"]),
        Index(value = ["tag_id"])
    ]
)
data class ScanTagCrossRef(
    @ColumnInfo(name = "scan_id")
    val scanId: Long,
    @ColumnInfo(name = "tag_id")
    val tagId: Long
)
