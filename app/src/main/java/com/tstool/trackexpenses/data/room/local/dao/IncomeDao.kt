package com.tstool.trackexpenses.data.room.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tstool.trackexpenses.data.room.entity.IncomeEntity

@Dao
interface IncomeDao {
    @Query("SELECT * FROM incomes_db ORDER BY date DESC")
    suspend fun getAll(): List<IncomeEntity>

    @Query("SELECT * FROM incomes_db WHERE id = :id")
    suspend fun getById(id: Int): IncomeEntity

    @Query("SELECT * FROM incomes_db WHERE source LIKE :query OR category LIKE :query OR note LIKE :query")
    suspend fun search(query: String): List<IncomeEntity>

    @Insert
    suspend fun insert(income: IncomeEntity): Long

    @Update
    suspend fun update(income: IncomeEntity)

    @Delete
    suspend fun delete(income: IncomeEntity)
}
