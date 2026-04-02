package com.voiceai.app.data.repository

import com.voiceai.app.data.local.dao.ScannedDocumentDao
import com.voiceai.app.data.local.dao.ScannedPageDao
import com.voiceai.app.data.local.dao.TagDao
import com.voiceai.app.data.local.entity.ScanTagCrossRef
import com.voiceai.app.data.local.entity.ScannedDocumentEntity
import com.voiceai.app.data.local.entity.ScannedPageEntity
import com.voiceai.app.domain.model.ScannedDocument
import com.voiceai.app.domain.model.ScannedPage
import com.voiceai.app.domain.model.Tag
import com.voiceai.app.domain.repository.ScannedDocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScannedDocumentRepositoryImpl @Inject constructor(
    private val scannedDocumentDao: ScannedDocumentDao,
    private val scannedPageDao: ScannedPageDao,
    private val tagDao: TagDao
) : ScannedDocumentRepository {

    override fun getAllDocuments(): Flow<List<ScannedDocument>> {
        return scannedDocumentDao.getAll().map { entities ->
            entities.map { entity ->
                val pages = scannedPageDao.getByDocumentId(entity.id).first()
                val tags = tagDao.getTagsForScan(entity.id).first()
                entity.toDomain(
                    pages = pages.map { it.toDomain() },
                    tags = tags.map { it.toDomain() }
                )
            }
        }
    }

    override suspend fun getDocumentById(id: Long): ScannedDocument? {
        val entity = scannedDocumentDao.getById(id) ?: return null
        val pages = scannedPageDao.getByDocumentId(id).first()
        val tags = tagDao.getTagsForScan(id).first()
        return entity.toDomain(
            pages = pages.map { it.toDomain() },
            tags = tags.map { it.toDomain() }
        )
    }

    override fun searchDocuments(query: String): Flow<List<ScannedDocument>> {
        return scannedDocumentDao.search(query).map { entities ->
            entities.map { entity ->
                val pages = scannedPageDao.getByDocumentId(entity.id).first()
                val tags = tagDao.getTagsForScan(entity.id).first()
                entity.toDomain(
                    pages = pages.map { it.toDomain() },
                    tags = tags.map { it.toDomain() }
                )
            }
        }
    }

    override suspend fun insertDocument(document: ScannedDocument): Long {
        val id = scannedDocumentDao.insert(document.toEntity())
        document.pages.forEach { page ->
            scannedPageDao.insert(page.toEntity().copy(documentId = id))
        }
        document.tags.forEach { tag ->
            val tagId = if (tag.id == 0L) {
                tagDao.insert(tag.toEntity())
            } else {
                tag.id
            }
            tagDao.insertScanTag(ScanTagCrossRef(scanId = id, tagId = tagId))
        }
        return id
    }

    override suspend fun updateDocument(document: ScannedDocument) {
        scannedDocumentDao.update(document.toEntity())
        // Update tag associations
        val existingTags = tagDao.getTagsForScan(document.id).first()
        existingTags.forEach { tag ->
            tagDao.deleteScanTag(ScanTagCrossRef(scanId = document.id, tagId = tag.id))
        }
        document.tags.forEach { tag ->
            val tagId = if (tag.id == 0L) {
                tagDao.insert(tag.toEntity())
            } else {
                tag.id
            }
            tagDao.insertScanTag(ScanTagCrossRef(scanId = document.id, tagId = tagId))
        }
    }

    override suspend fun deleteDocument(id: Long) {
        scannedDocumentDao.deleteById(id)
    }

    override suspend fun toggleFavorite(id: Long) {
        val document = scannedDocumentDao.getById(id) ?: return
        scannedDocumentDao.update(document.copy(isFavorite = !document.isFavorite))
    }

    override fun getPages(documentId: Long): Flow<List<ScannedPage>> {
        return scannedPageDao.getByDocumentId(documentId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertPage(page: ScannedPage): Long {
        return scannedPageDao.insert(page.toEntity())
    }

    override suspend fun deletePage(id: Long) {
        val page = scannedPageDao.getById(id) ?: return
        scannedPageDao.delete(page)
    }
}

fun ScannedDocumentEntity.toDomain(
    pages: List<ScannedPage> = emptyList(),
    tags: List<Tag> = emptyList()
): ScannedDocument {
    return ScannedDocument(
        id = id,
        title = title,
        extractedText = extractedText,
        summary = summary,
        pageCount = pageCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        language = language,
        isFavorite = isFavorite,
        folderId = folderId,
        pdfFilePath = pdfFilePath,
        pages = pages,
        tags = tags
    )
}

fun ScannedDocument.toEntity(): ScannedDocumentEntity {
    return ScannedDocumentEntity(
        id = id,
        title = title,
        extractedText = extractedText,
        summary = summary,
        pageCount = pageCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        language = language,
        isFavorite = isFavorite,
        folderId = folderId,
        pdfFilePath = pdfFilePath
    )
}

fun ScannedPageEntity.toDomain(): ScannedPage {
    return ScannedPage(
        id = id,
        documentId = documentId,
        pageNumber = pageNumber,
        imagePath = imagePath,
        rawImagePath = rawImagePath,
        pageText = pageText,
        filter = filter
    )
}

fun ScannedPage.toEntity(): ScannedPageEntity {
    return ScannedPageEntity(
        id = id,
        documentId = documentId,
        pageNumber = pageNumber,
        imagePath = imagePath,
        rawImagePath = rawImagePath,
        pageText = pageText,
        filter = filter
    )
}
