package com.voiceai.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.voiceai.app.data.local.entity.ScannedDocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScannedDocumentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(document: ScannedDocumentEntity): Long

    @Update
    suspend fun update(document: ScannedDocumentEntity)

    @Delete
    suspend fun delete(document: ScannedDocumentEntity)

    @Query("SELECT * FROM scanned_documents ORDER BY created_at DESC")
    fun getAll(): Flow<List<ScannedDocumentEntity>>

    @Query("SELECT * FROM scanned_documents WHERE id = :id")
    suspend fun getById(id: Long): ScannedDocumentEntity?

    @Query("SELECT * FROM scanned_documents WHERE title LIKE '%' || :query || '%' OR extracted_text LIKE '%' || :query || '%' ORDER BY created_at DESC")
    fun search(query: String): Flow<List<ScannedDocumentEntity>>

    @Query("SELECT * FROM scanned_documents WHERE folder_id = :folderId ORDER BY created_at DESC")
    fun getByFolder(folderId: Long): Flow<List<ScannedDocumentEntity>>

    @Query("SELECT * FROM scanned_documents WHERE is_favorite = 1 ORDER BY created_at DESC")
    fun getFavorites(): Flow<List<ScannedDocumentEntity>>

    @Query("DELETE FROM scanned_documents WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM scanned_documents WHERE document_type = :type ORDER BY created_at DESC")
    fun getByDocumentType(type: String): Flow<List<ScannedDocumentEntity>>
}
