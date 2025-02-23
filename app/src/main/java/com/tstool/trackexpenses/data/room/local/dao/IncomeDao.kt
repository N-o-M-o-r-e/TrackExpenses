package com.tstool.trackexpenses.data.room.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tstool.trackexpenses.data.room.entity.IncomeEntity
import com.tstool.trackexpenses.data.room.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Insert
    suspend fun insertIncome(income: IncomeEntity)

    @Update
    suspend fun updateIncome(income: IncomeEntity)

    @Delete
    suspend fun deleteIncome(income: IncomeEntity)

    @Query("SELECT * FROM incomes ORDER BY date DESC")
    fun getAllIncomes(): Flow<List<IncomeEntity>>

    @Query("SELECT SUM(amount) FROM incomes")
    fun getTotalIncome(): Flow<Double?>

    // Tìm kiếm theo nguồn thu nhập
    @Query("SELECT * FROM incomes WHERE source LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchIncomes(query: String): Flow<List<IncomeEntity>>



}

