package com.tstool.trackexpenses.data.room.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses_db ORDER BY date DESC")
    fun getAll(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses_db WHERE id = :id")
    fun getById(id: Int): Flow<ExpenseEntity?>

    @Query("SELECT * FROM expenses_db WHERE category LIKE '%' || :query || '%' OR item_name LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses_db WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT price FROM expenses_db WHERE category = :category")
    fun getPricesByCategory(category: String): Flow<List<Double>>

    @Query("SELECT * FROM expenses_db WHERE date >= :dayStart AND date < :dayEnd ORDER BY date DESC")
    fun getByDay(dayStart: Long, dayEnd: Long): Flow<List<ExpenseEntity>>

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)
}