package com.tstool.trackexpenses.data.room.repository

import com.tstool.trackexpenses.data.room.entity.IncomeEntity
import com.tstool.trackexpenses.data.room.local.dao.IncomeDao

class IncomeRepository(private val incomeDao: IncomeDao) {
    suspend fun getAllIncomes(): List<IncomeEntity> = incomeDao.getAll()
    suspend fun getIncomeById(id: Int): IncomeEntity = incomeDao.getById(id)
    suspend fun searchIncomes(query: String): List<IncomeEntity> = incomeDao.search("%$query%")
    suspend fun insertIncome(income: IncomeEntity): Long = incomeDao.insert(income)
    suspend fun updateIncome(income: IncomeEntity) = incomeDao.update(income)
    suspend fun deleteIncome(income: IncomeEntity) = incomeDao.delete(income)
}