package com.tstool.trackexpenses.data.room.repository

import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import com.tstool.trackexpenses.data.room.local.dao.ExpenseDao
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    suspend fun getAllExpenses(): List<ExpenseEntity> = expenseDao.getAll()
    suspend fun getExpenseById(id: Int): ExpenseEntity? = expenseDao.getById(id)
    suspend fun searchExpenses(query: String): List<ExpenseEntity> = expenseDao.search("%$query%")
    suspend fun insertExpense(expense: ExpenseEntity): Long = expenseDao.insert(expense)
    suspend fun updateExpense(expense: ExpenseEntity) = expenseDao.update(expense)
    suspend fun deleteExpense(expense: ExpenseEntity) = expenseDao.delete(expense)
}