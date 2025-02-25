package com.tstool.trackexpenses.data.room.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tstool.trackexpenses.data.room.entity.TransactionEntity

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions_db ORDER BY date DESC")
    suspend fun getAll(): List<TransactionEntity>

    @Query("SELECT * FROM transactions_db WHERE id = :id")
    suspend fun getById(id: Int): TransactionEntity

    @Query("SELECT * FROM transactions_db WHERE description LIKE :query OR type LIKE :query")
    suspend fun search(query: String): List<TransactionEntity>

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)
}

