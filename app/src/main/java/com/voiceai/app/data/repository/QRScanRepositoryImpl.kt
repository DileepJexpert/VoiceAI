package com.voiceai.app.data.repository

import com.voiceai.app.data.local.dao.QRScanDao
import com.voiceai.app.data.local.entity.QRScanEntity
import com.voiceai.app.domain.model.QRScanResult
import com.voiceai.app.domain.repository.QRScanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QRScanRepositoryImpl @Inject constructor(
    private val qrScanDao: QRScanDao
) : QRScanRepository {

    override fun getAll(): Flow<List<QRScanResult>> {
        return qrScanDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getByType(type: String): Flow<List<QRScanResult>> {
        return qrScanDao.getByType(type).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insert(result: QRScanResult): Long {
        return qrScanDao.insert(result.toEntity())
    }

    override suspend fun delete(id: Long) {
        qrScanDao.deleteById(id)
    }

    override suspend fun clearAll() {
        qrScanDao.clearAll()
    }

    private fun QRScanEntity.toDomain(): QRScanResult {
        return QRScanResult(
            id = id,
            content = content,
            type = type,
            createdAt = createdAt
        )
    }

    private fun QRScanResult.toEntity(): QRScanEntity {
        return QRScanEntity(
            id = id,
            content = content,
            type = type,
            createdAt = createdAt
        )
    }
}
