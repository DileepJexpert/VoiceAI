package com.voiceai.app.domain.model

data class ScannedPage(
    val id: Long,
    val documentId: Long,
    val pageNumber: Int,
    val imagePath: String,
    val rawImagePath: String,
    val pageText: String?,
    val filter: String
)
