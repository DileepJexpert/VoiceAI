package com.voiceai.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.voiceai.app.data.local.entity.QRScanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QRScanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(qrScan: QRScanEntity): Long

    @Delete
    suspend fun delete(qrScan: QRScanEntity)

    @Query("SELECT * FROM qr_history ORDER BY created_at DESC")
    fun getAll(): Flow<List<QRScanEntity>>

    @Query("SELECT * FROM qr_history WHERE id = :id")
    suspend fun getById(id: Long): QRScanEntity?

    @Query("SELECT * FROM qr_history WHERE type = :type ORDER BY created_at DESC")
    fun getByType(type: String): Flow<List<QRScanEntity>>

    @Query("DELETE FROM qr_history")
    suspend fun clearAll()

    @Query("DELETE FROM qr_history WHERE id = :id")
    suspend fun deleteById(id: Long)
}
