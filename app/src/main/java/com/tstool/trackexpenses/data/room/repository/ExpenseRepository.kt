package com.tstool.trackexpenses.data.room.repository

import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import com.tstool.trackexpenses.data.room.local.dao.ExpenseDao
import kotlinx.coroutines.flow.Flow

import java.util.Calendar

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAll()

    fun getExpenseById(id: Int): Flow<ExpenseEntity?> = expenseDao.getById(id)

    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>> =
        expenseDao.getByDateRange(startDate, endDate)

    fun getPricesByCategory(category: String): Flow<List<Double>> =
        expenseDao.getPricesByCategory(category)

    fun getExpensesByDay(date: Long): Flow<List<ExpenseEntity>> {
        val calendar = getCalendarInstance(date)
        val dayStart = calendar.timeInMillis
        val dayEnd = dayStart + 24 * 60 * 60 * 1000
        return expenseDao.getByDay(dayStart, dayEnd)
    }

    suspend fun insertExpense(expense: ExpenseEntity): Long = expenseDao.insert(expense)

    suspend fun updateExpense(expense: ExpenseEntity) = expenseDao.update(expense)

    suspend fun deleteExpense(expense: ExpenseEntity) = expenseDao.delete(expense)

    fun searchExpenses(query: String): Flow<List<ExpenseEntity>> = expenseDao.search(query)
}

private fun getCalendarInstance(date: Long): Calendar {
    return Calendar.getInstance().apply {
        timeInMillis = date
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}