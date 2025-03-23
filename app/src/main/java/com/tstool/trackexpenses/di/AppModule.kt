package com.tstool.trackexpenses.di

import androidx.room.Room
import com.tstool.trackexpenses.data.room.local.AppDatabase
import com.tstool.trackexpenses.data.room.repository.ExpenseRepository
import com.tstool.trackexpenses.data.room.repository.IncomeRepository
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseViewModel
import com.tstool.trackexpenses.ui.view.viewmodel.IncomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            context = androidContext(), klass = AppDatabase::class.java, name = "app_database"
        ).fallbackToDestructiveMigration().build()
    }

    single { get<AppDatabase>().expenseDao() }
    single { get<AppDatabase>().incomeDao() }

    single { ExpenseRepository(get()) }
    single { IncomeRepository(get()) }

    viewModel { ExpenseViewModel(get()) }
    viewModel { IncomeViewModel(get()) }
}