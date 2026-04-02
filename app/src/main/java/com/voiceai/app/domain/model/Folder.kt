package com.voiceai.app.domain.model

data class Folder(
    val id: Long,
    val name: String,
    val icon: String,
    val isSmartFolder: Boolean = false,
    val sortRule: String? = null
)
