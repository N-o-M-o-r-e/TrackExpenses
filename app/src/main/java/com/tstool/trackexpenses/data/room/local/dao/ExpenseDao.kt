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
    @Insert
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalExpenses(): Flow<Double?>

    // Tìm kiếm theo tên món đồ hoặc danh mục chi tiêu
    @Query("SELECT * FROM expenses WHERE item_name LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchExpenses(query: String): Flow<List<ExpenseEntity>>

}

