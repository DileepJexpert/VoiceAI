package com.voiceai.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.voiceai.app.data.local.entity.ScannedContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScannedContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ScannedContactEntity): Long

    @Update
    suspend fun update(contact: ScannedContactEntity)

    @Delete
    suspend fun delete(contact: ScannedContactEntity)

    @Query("SELECT * FROM scanned_contacts ORDER BY created_at DESC")
    fun getAll(): Flow<List<ScannedContactEntity>>

    @Query("SELECT * FROM scanned_contacts WHERE id = :id")
    suspend fun getById(id: Long): ScannedContactEntity?

    @Query("SELECT * FROM scanned_contacts WHERE scan_id = :scanId")
    fun getByScanId(scanId: Long): Flow<List<ScannedContactEntity>>

    @Query("SELECT * FROM scanned_contacts WHERE name LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR company LIKE '%' || :query || '%' ORDER BY created_at DESC")
    fun search(query: String): Flow<List<ScannedContactEntity>>

    @Query("DELETE FROM scanned_contacts WHERE id = :id")
    suspend fun deleteById(id: Long)
}
