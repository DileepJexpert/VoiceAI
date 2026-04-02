package com.voiceai.app.domain.repository

import com.voiceai.app.domain.model.QRScanResult
import kotlinx.coroutines.flow.Flow

interface QRScanRepository {
    fun getAll(): Flow<List<QRScanResult>>
    fun getByType(type: String): Flow<List<QRScanResult>>
    suspend fun insert(result: QRScanResult): Long
    suspend fun delete(id: Long)
    suspend fun clearAll()
}
