package com.tstool.trackexpenses.data.room.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.tstool.trackexpenses.data.room.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Query("SELECT * FROM income_db ORDER BY date DESC")
    fun getAll(): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM income_db WHERE id = :id")
    fun getById(id: Int): Flow<IncomeEntity?>

    @Query("SELECT * FROM income_db WHERE category LIKE '%' || :query || '%' OR source_name LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM income_db WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<IncomeEntity>>

    @Query("SELECT amount FROM income_db WHERE category = :category")
    fun getAmountsByCategory(category: String): Flow<List<Double>>

    @Query("SELECT * FROM income_db WHERE date >= :dayStart AND date < :dayEnd ORDER BY date DESC")
    fun getByDay(dayStart: Long, dayEnd: Long): Flow<List<IncomeEntity>>

    @Insert
    suspend fun insert(income: IncomeEntity): Long

    @Update
    suspend fun update(income: IncomeEntity)

    @Delete
    suspend fun delete(income: IncomeEntity)
}