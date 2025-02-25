package com.tstool.trackexpenses.ui.view.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import com.tstool.trackexpenses.data.room.entity.IncomeEntity
import com.tstool.trackexpenses.data.room.entity.TransactionEntity
import com.tstool.trackexpenses.data.room.local.dao.ExpenseDao
import com.tstool.trackexpenses.data.room.local.dao.IncomeDao
import com.tstool.trackexpenses.data.room.local.dao.TransactionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FinanceViewModel(
    private val expenseDao: ExpenseDao,
    private val incomeDao: IncomeDao,
    private val transactionDao: TransactionDao
) : ViewModel() {

    private val _eventCreateExpenses = MutableLiveData<Unit>()
    val eventCreateExpenses: LiveData<Unit> get() = _eventCreateExpenses

    fun triggerCreateExpensesEvent() {
        _eventCreateExpenses.value = Unit
    }
}


