package com.tstool.trackexpenses.data.room.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import com.tstool.trackexpenses.data.room.entity.IncomeEntity
import com.tstool.trackexpenses.data.room.entity.TransactionEntity
import com.tstool.trackexpenses.data.room.local.dao.ExpenseDao
import com.tstool.trackexpenses.data.room.local.dao.IncomeDao
import com.tstool.trackexpenses.data.room.local.dao.TransactionDao

@Database(entities = [ExpenseEntity::class, IncomeEntity::class, TransactionEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
    abstract fun transactionDao(): TransactionDao
}
