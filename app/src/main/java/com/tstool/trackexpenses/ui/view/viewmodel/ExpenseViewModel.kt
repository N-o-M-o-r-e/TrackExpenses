package com.tstool.trackexpenses.ui.view.viewmodel

import androidx.lifecycle.viewModelScope
import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import com.tstool.trackexpenses.data.room.repository.ExpenseRepository
import com.tstool.trackexpenses.utils.base.BaseViewModel
import kotlinx.coroutines.launch

class ExpenseViewModel(private val repository: ExpenseRepository) :
    BaseViewModel<ExpenseUiState, ExpenseAction, ExpenseEvent>() {

    override fun initUiState(): ExpenseUiState = ExpenseUiState()

    init {
        viewModelScope.launch {
            loadExpenses()
            handleActions()
        }
    }

    private suspend fun loadExpenses() {
        val expenses = repository.getAllExpenses()
        mutableStateFlow.value = uiState.copy(expenses = expenses)
    }

    private suspend fun handleActions() {
        actionSharedFlow.collect { action ->
            when (action) {
                is ExpenseAction.Add -> {
                    val id = repository.insertExpense(action.expense)
                    if (id > 0) {
                        loadExpenses()
                        sendEvent(ExpenseEvent.ShowSuccess("Added expense"))
                    }
                }
                is ExpenseAction.Update -> {
                    repository.updateExpense(action.expense)
                    loadExpenses()
                    sendEvent(ExpenseEvent.ShowSuccess("Updated expense"))
                }
                is ExpenseAction.Delete -> {
                    repository.deleteExpense(action.expense)
                    loadExpenses()
                    sendEvent(ExpenseEvent.ShowSuccess("Deleted expense"))
                }
                is ExpenseAction.Search -> {
                    val results = repository.searchExpenses(action.query)
                    mutableStateFlow.value = uiState.copy(expenses = results)
                }
            }
        }
    }
}

// UI State
data class ExpenseUiState(
    val expenses: List<ExpenseEntity> = emptyList(),
    val isLoading: Boolean = false
)

// Actions
sealed class ExpenseAction {
    data class Add(val expense: ExpenseEntity) : ExpenseAction()
    data class Update(val expense: ExpenseEntity) : ExpenseAction()
    data class Delete(val expense: ExpenseEntity) : ExpenseAction()
    data class Search(val query: String) : ExpenseAction()
}

// Events
sealed class ExpenseEvent {
    data class ShowSuccess(val message: String) : ExpenseEvent()
}