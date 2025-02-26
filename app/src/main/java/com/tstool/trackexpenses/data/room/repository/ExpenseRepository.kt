package com.tstool.trackexpenses.data.room.repository

import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import com.tstool.trackexpenses.data.room.local.dao.ExpenseDao

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    suspend fun getAllExpenses(): Result<List<ExpenseEntity>> = try {
        Result.success(expenseDao.getAll())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getExpenseById(id: Int): Result<ExpenseEntity?> = try {
        Result.success(expenseDao.getById(id))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun searchExpenses(query: String): Result<List<ExpenseEntity>> = try {
        Result.success(expenseDao.search(query))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getExpensesByDateRange(startDate: Long, endDate: Long): Result<List<ExpenseEntity>> = try {
        Result.success(expenseDao.getByDateRange(startDate, endDate))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getPricesByCategory(category: String): Result<List<Double>> = try {
        Result.success(expenseDao.getPricesByCategory(category))
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Thêm hàm mới: Lấy danh sách expense trong một ngày
    suspend fun getExpensesByDay(day: Long): Result<List<ExpenseEntity>> = try {
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = day
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val dayStart = calendar.timeInMillis
        val dayEnd = dayStart + 24 * 60 * 60 * 1000 // Cộng 1 ngày (86,400,000 ms)
        Result.success(expenseDao.getByDay(dayStart, dayEnd))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun insertExpense(expense: ExpenseEntity): Result<Long> = try {
        val id = expenseDao.insert(expense)
        Result.success(id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateExpense(expense: ExpenseEntity): Result<Unit> = try {
        expenseDao.update(expense)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteExpense(expense: ExpenseEntity): Result<Unit> = try {
        expenseDao.delete(expense)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}