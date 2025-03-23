package com.tstool.trackexpenses.data.room.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tstool.trackexpenses.data.room.local.dao.IncomeDao
import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import com.tstool.trackexpenses.data.room.entity.IncomeEntity
import com.tstool.trackexpenses.data.room.local.dao.ExpenseDao

@Database(
    entities = [ExpenseEntity::class, IncomeEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
}