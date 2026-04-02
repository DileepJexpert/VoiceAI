package com.voiceai.app.domain.model

data class ScannedContact(
    val id: Long,
    val name: String,
    val phone: String?,
    val email: String?,
    val company: String?,
    val designation: String?,
    val address: String?,
    val website: String?,
    val scanId: Long?,
    val imagePath: String?,
    val isSavedToContacts: Boolean,
    val createdAt: Long
)
