package com.voiceai.app.domain.repository

import com.voiceai.app.domain.model.ScannedContact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun getAll(): Flow<List<ScannedContact>>
    suspend fun getById(id: Long): ScannedContact?
    fun search(query: String): Flow<List<ScannedContact>>
    suspend fun insert(contact: ScannedContact): Long
    suspend fun update(contact: ScannedContact)
    suspend fun delete(id: Long)
    suspend fun saveToDeviceContacts(contact: ScannedContact): Boolean
}
