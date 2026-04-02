package com.voiceai.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.voiceai.app.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

data class CategoryTotal(
    val category: String,
    val total: Double
)

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAll(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE scan_id = :scanId")
    fun getByScanId(scanId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    fun getByCategory(category: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE date >= :start AND date <= :end ORDER BY date DESC")
    fun getByDateRange(start: Long, end: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE category IS NOT NULL GROUP BY category")
    fun getTotalByCategory(): Flow<List<CategoryTotal>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE strftime('%Y', date / 1000, 'unixepoch') = :year AND strftime('%m', date / 1000, 'unixepoch') = :month")
    fun getMonthlyTotal(year: String, month: String): Flow<Double>

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Long)
}
