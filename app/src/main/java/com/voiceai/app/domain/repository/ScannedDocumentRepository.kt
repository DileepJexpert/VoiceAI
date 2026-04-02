package com.voiceai.app.domain.repository

import com.voiceai.app.domain.model.ScannedDocument
import com.voiceai.app.domain.model.ScannedPage
import kotlinx.coroutines.flow.Flow

interface ScannedDocumentRepository {
    fun getAllDocuments(): Flow<List<ScannedDocument>>
    suspend fun getDocumentById(id: Long): ScannedDocument?
    fun searchDocuments(query: String): Flow<List<ScannedDocument>>
    suspend fun insertDocument(document: ScannedDocument): Long
    suspend fun updateDocument(document: ScannedDocument)
    suspend fun deleteDocument(id: Long)
    suspend fun toggleFavorite(id: Long)
    fun getPages(documentId: Long): Flow<List<ScannedPage>>
    suspend fun insertPage(page: ScannedPage): Long
    suspend fun deletePage(id: Long)
}
