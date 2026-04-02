package com.voiceai.app.domain.model

data class QRScanResult(
    val id: Long,
    val content: String,
    val type: String,
    val createdAt: Long
)
