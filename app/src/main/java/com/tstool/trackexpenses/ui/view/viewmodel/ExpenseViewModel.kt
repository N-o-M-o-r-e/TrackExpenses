package com.tstool.trackexpenses.ui.view.viewmodel


import android.util.Log
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
            Log.i("ExpenseViewModel", "Initializing ViewModel, loading expenses")
            loadExpenses()
            handleActions()
        }
    }

    private suspend fun loadExpenses() {
        Log.i("ExpenseViewModel", "Loading expenses from database")
        mutableStateFlow.value = uiState.copy(isLoading = true)
        repository.getAllExpenses().fold(
            onSuccess = { expenses ->
                Log.i("ExpenseViewModel", "Loaded ${expenses.size} expenses")
                mutableStateFlow.value = uiState.copy(expenses = expenses, isLoading = false)
            },
            onFailure = { error ->
                Log.e("ExpenseViewModel", "Failed to load expenses: ${error.message}")
                mutableStateFlow.value = uiState.copy(isLoading = false)
                sendEvent(ExpenseEvent.ShowError("Failed to load expenses: ${error.message}"))
            }
        )
    }

    private suspend fun handleActions() {
        actionSharedFlow.collect { action ->
            when (action) {
                is ExpenseAction.Add -> {
                    Log.i("ExpenseViewModel", "Handling Add action: ${action.expense}")
                    repository.insertExpense(action.expense).fold(
                        onSuccess = { id ->
                            if (id > 0) {
                                Log.i("ExpenseViewModel", "Expense inserted with ID: $id")
                                sendEvent(ExpenseEvent.ShowSuccess("Added expense")) // Gửi sự kiện trước
                                loadExpenses() // Tải lại sau, không chờ
                            } else {
                                Log.w("ExpenseViewModel", "Insert returned invalid ID: $id")
                            }
                        },
                        onFailure = { error ->
                            Log.e("ExpenseViewModel", "Failed to insert expense: ${error.message}")
                            sendEvent(ExpenseEvent.ShowError("Failed to add: ${error.message}"))
                        }
                    )
                }
                is ExpenseAction.Update -> {
                    Log.i("ExpenseViewModel", "Handling Update action: ${action.expense}")
                    repository.updateExpense(action.expense).fold(
                        onSuccess = {
                            Log.i("ExpenseViewModel", "Expense updated")
                            sendEvent(ExpenseEvent.ShowSuccess("Updated expense"))
                            loadExpenses()
                        },
                        onFailure = { error ->
                            Log.e("ExpenseViewModel", "Failed to update expense: ${error.message}")
                            sendEvent(ExpenseEvent.ShowError("Failed to update: ${error.message}"))
                        }
                    )
                }
                is ExpenseAction.Delete -> {
                    Log.i("ExpenseViewModel", "Handling Delete action: ${action.expense}")
                    repository.deleteExpense(action.expense).fold(
                        onSuccess = {
                            Log.i("ExpenseViewModel", "Expense deleted")
                            sendEvent(ExpenseEvent.ShowSuccess("Deleted expense"))
                            loadExpenses()
                        },
                        onFailure = { error ->
                            Log.e("ExpenseViewModel", "Failed to delete expense: ${error.message}")
                            sendEvent(ExpenseEvent.ShowError("Failed to delete: ${error.message}"))
                        }
                    )
                }
                is ExpenseAction.Search -> {
                    Log.i("ExpenseViewModel", "Handling Search action: ${action.query}")
                    repository.searchExpenses(action.query).fold(
                        onSuccess = { results ->
                            Log.i("ExpenseViewModel", "Search returned ${results.size} results")
                            mutableStateFlow.value = uiState.copy(expenses = results)
                        },
                        onFailure = { error ->
                            Log.e("ExpenseViewModel", "Search failed: ${error.message}")
                            sendEvent(ExpenseEvent.ShowError("Search failed: ${error.message}"))
                        }
                    )
                }
                is ExpenseAction.FilterByDateRange -> {
                    Log.i("ExpenseViewModel", "Handling FilterByDateRange action: ${action.startDate} - ${action.endDate}")
                    repository.getExpensesByDateRange(action.startDate, action.endDate).fold(
                        onSuccess = { results ->
                            Log.i("ExpenseViewModel", "Filter returned ${results.size} results")
                            mutableStateFlow.value = uiState.copy(expenses = results)
                        },
                        onFailure = { error ->
                            Log.e("ExpenseViewModel", "Failed to filter: ${error.message}")
                            sendEvent(ExpenseEvent.ShowError("Filter failed: ${error.message}"))
                        }
                    )
                }
                is ExpenseAction.GetPricesByCategory -> {
                    Log.i("ExpenseViewModel", "Handling GetPricesByCategory action: ${action.category}")
                    repository.getPricesByCategory(action.category).fold(
                        onSuccess = { prices ->
                            Log.i("ExpenseViewModel", "Got prices: $prices")
                            sendEvent(ExpenseEvent.ShowPrices(action.category, prices))
                        },
                        onFailure = { error ->
                            Log.e("ExpenseViewModel", "Failed to get prices: ${error.message}")
                            sendEvent(ExpenseEvent.ShowError("Failed to get prices: ${error.message}"))
                        }
                    )
                }
                is ExpenseAction.GetByDay -> {
                    Log.i("ExpenseViewModel", "Handling GetByDay action: ${action.day}")
                    repository.getExpensesByDay(action.day).fold(
                        onSuccess = { expenses ->
                            Log.i("ExpenseViewModel", "Got ${expenses.size} expenses for day")
                            mutableStateFlow.value = uiState.copy(expenses = expenses)
                            sendEvent(ExpenseEvent.ShowSuccess("Loaded expenses for the day"))
                        },
                        onFailure = { error ->
                            Log.e("ExpenseViewModel", "Failed to load day expenses: ${error.message}")
                            sendEvent(ExpenseEvent.ShowError("Failed to load day expenses: ${error.message}"))
                        }
                    )
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
    data class FilterByDateRange(val startDate: Long, val endDate: Long) : ExpenseAction()
    data class GetPricesByCategory(val category: String) : ExpenseAction()
    // Thêm action mới
    data class GetByDay(val day: Long) : ExpenseAction()
}

// Events
sealed class ExpenseEvent {
    data class ShowSuccess(val message: String) : ExpenseEvent()
    data class ShowError(val message: String) : ExpenseEvent()
    data class ShowPrices(val category: String, val prices: List<Double>) : ExpenseEvent()
}