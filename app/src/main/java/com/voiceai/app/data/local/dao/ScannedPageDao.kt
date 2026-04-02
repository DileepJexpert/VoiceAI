package com.voiceai.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.voiceai.app.data.local.entity.ScannedPageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScannedPageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(page: ScannedPageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pages: List<ScannedPageEntity>)

    @Update
    suspend fun update(page: ScannedPageEntity)

    @Delete
    suspend fun delete(page: ScannedPageEntity)

    @Query("SELECT * FROM scanned_pages WHERE document_id = :documentId ORDER BY page_number ASC")
    fun getByDocumentId(documentId: Long): Flow<List<ScannedPageEntity>>

    @Query("SELECT * FROM scanned_pages WHERE id = :id")
    suspend fun getById(id: Long): ScannedPageEntity?

    @Query("DELETE FROM scanned_pages WHERE document_id = :documentId")
    suspend fun deleteByDocumentId(documentId: Long)
}
