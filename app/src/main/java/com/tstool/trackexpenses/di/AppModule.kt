package com.tstool.trackexpenses.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import androidx.room.Room
import com.tstool.trackexpenses.data.room.local.AppDatabase
import com.tstool.trackexpenses.data.room.repository.ExpenseRepository
import com.tstool.trackexpenses.data.room.repository.IncomeRepository
import com.tstool.trackexpenses.data.room.repository.TransactionRepository
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseViewModel
import com.tstool.trackexpenses.ui.view.viewmodel.IncomeViewModel
import com.tstool.trackexpenses.ui.view.viewmodel.TransactionViewModel

val appModule = module {
    // Database
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "app_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    // DAOs
    single { get<AppDatabase>().expenseDao() }
    single { get<AppDatabase>().incomeDao() }
    single { get<AppDatabase>().transactionDao() }

    // Repositories
    single { ExpenseRepository(get()) }
    single { IncomeRepository(get()) }
    single { TransactionRepository(get()) }

    // ViewModels
    single { ExpenseViewModel(get()) } // Đổi thành single để scope ở mức ứng dụng
    viewModel { IncomeViewModel(get()) }
    viewModel { TransactionViewModel(get()) }
}