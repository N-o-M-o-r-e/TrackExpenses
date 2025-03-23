package com.tstool.trackexpenses.data.room.repository

import com.tstool.trackexpenses.data.room.local.dao.IncomeDao
import com.tstool.trackexpenses.data.room.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class IncomeRepository(private val incomeDao: IncomeDao) {
    fun getAllIncome(): Flow<List<IncomeEntity>> = incomeDao.getAll()

    fun getIncomeById(id: Int): Flow<IncomeEntity?> = incomeDao.getById(id)

    fun getIncomeByDateRange(startDate: Long, endDate: Long): Flow<List<IncomeEntity>> =
        incomeDao.getByDateRange(startDate, endDate)

    fun getAmountsByCategory(category: String): Flow<List<Double>> =
        incomeDao.getAmountsByCategory(category)

    fun getIncomeByDay(date: Long): Flow<List<IncomeEntity>> {
        val calendar = getCalendarInstance(date)
        val dayStart = calendar.timeInMillis
        val dayEnd = dayStart + 24 * 60 * 60 * 1000
        return incomeDao.getByDay(dayStart, dayEnd)
    }

    // Thêm hàm mới
    fun getIncomeByCategoryAndDateRange(
        category: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<IncomeEntity>> = incomeDao.getIncomeByCategoryAndDateRange(category, startDate, endDate)

    suspend fun insertIncome(income: IncomeEntity): Long = incomeDao.insert(income)

    suspend fun updateIncome(income: IncomeEntity) = incomeDao.update(income)

    suspend fun deleteIncome(income: IncomeEntity) = incomeDao.delete(income)

    fun searchIncome(query: String): Flow<List<IncomeEntity>> = incomeDao.search(query)
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