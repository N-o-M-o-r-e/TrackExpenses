package com.tstool.trackexpenses.ui.view.viewmodel

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

    private val _expenses = MutableStateFlow<List<ExpenseEntity>>(emptyList())
    val expenses: StateFlow<List<ExpenseEntity>> = _expenses

    private val _totalExpenses = MutableStateFlow(0.0)
    val totalExpenses: StateFlow<Double> = _totalExpenses

    private val _incomes = MutableStateFlow<List<IncomeEntity>>(emptyList())
    val incomes: StateFlow<List<IncomeEntity>> = _incomes

    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome

    private val _transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactions: StateFlow<List<TransactionEntity>> = _transactions

    private val _totalSent = MutableStateFlow(0.0)
    val totalSent: StateFlow<Double> = _totalSent

    private val _totalReceived = MutableStateFlow(0.0)
    val totalReceived: StateFlow<Double> = _totalReceived

    init {
        viewModelScope.launch {
            expenseDao.getAllExpenses().collect { _expenses.value = it }
        }
        viewModelScope.launch {
            expenseDao.getTotalExpenses().collect { _totalExpenses.value = it ?: 0.0 }
        }
        viewModelScope.launch {
            incomeDao.getAllIncomes().collect { _incomes.value = it }
        }
        viewModelScope.launch {
            incomeDao.getTotalIncome().collect { _totalIncome.value = it ?: 0.0 }
        }
        viewModelScope.launch {
            transactionDao.getAllTransactions().collect { _transactions.value = it }
        }
        viewModelScope.launch {
            transactionDao.getTotalSent().collect { _totalSent.value = it ?: 0.0 }
        }
        viewModelScope.launch {
            transactionDao.getTotalReceived().collect { _totalReceived.value = it ?: 0.0 }
        }
    }

    // ========================== CHI TIÊU (EXPENSES) ==========================

    fun insertExpense(expense: ExpenseEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseDao.insertExpense(expense)
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseDao.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseDao.deleteExpense(expense)
        }
    }

    fun searchExpenses(query: String) {
        viewModelScope.launch {
            expenseDao.searchExpenses("%$query%").collect { _expenses.value = it }
        }
    }

    // ========================== THU NHẬP (INCOME) ==========================

    fun insertIncome(income: IncomeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            incomeDao.insertIncome(income)
        }
    }

    fun updateIncome(income: IncomeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            incomeDao.updateIncome(income)
        }
    }

    fun deleteIncome(income: IncomeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            incomeDao.deleteIncome(income)
        }
    }

    fun searchIncomes(query: String) {
        viewModelScope.launch {
            incomeDao.searchIncomes("%$query%").collect { _incomes.value = it }
        }
    }

    // ========================== GIAO DỊCH (TRANSACTIONS) ==========================

    fun insertTransaction(transaction: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.insertTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.deleteTransaction(transaction)
        }
    }

    fun searchTransactions(query: String) {
        viewModelScope.launch {
            transactionDao.searchTransactions("%$query%").collect { _transactions.value = it }
        }
    }
}


