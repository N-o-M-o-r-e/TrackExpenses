package com.tstool.trackexpenses.di

import androidx.room.Room
import com.tstool.trackexpenses.data.room.local.AppDatabase
import com.tstool.trackexpenses.ui.view.viewmodel.FinanceViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Database Instance
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "finance_tracker_db")
            .build()
    }

    // DAOs
    single { get<AppDatabase>().expenseDao() }
    single { get<AppDatabase>().incomeDao() }
    single { get<AppDatabase>().transactionDao() }

    // ViewModels
    viewModel { FinanceViewModel(get(), get(), get()) }
}
