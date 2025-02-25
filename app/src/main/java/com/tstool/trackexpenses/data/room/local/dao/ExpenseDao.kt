package com.tstool.trackexpenses.data.room.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tstool.trackexpenses.data.room.entity.ExpenseEntity

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses_db ORDER BY date DESC")
    suspend fun getAll(): List<ExpenseEntity>

    @Query("SELECT * FROM expenses_db WHERE id = :id")
    suspend fun getById(id: Int): ExpenseEntity?

    @Query("SELECT * FROM expenses_db WHERE category LIKE :query OR item_name LIKE :query OR note LIKE :query")
    suspend fun search(query: String): List<ExpenseEntity>

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)
}
