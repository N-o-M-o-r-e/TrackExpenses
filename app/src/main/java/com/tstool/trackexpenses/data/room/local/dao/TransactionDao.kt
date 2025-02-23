package com.tstool.trackexpenses.data.room.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tstool.trackexpenses.data.room.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'send'")
    fun getTotalSent(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'receive'")
    fun getTotalReceived(): Flow<Double?>

    // Tìm kiếm giao dịch theo nội dung
    @Query("SELECT * FROM transactions WHERE description LIKE :query ORDER BY date DESC")
    fun searchTransactions(query: String): Flow<List<TransactionEntity>>
}

