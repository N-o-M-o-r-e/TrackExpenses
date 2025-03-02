package com.tstool.trackexpenses.ui.view.viewmodel


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import com.tstool.trackexpenses.data.room.repository.ExpenseRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ExpenseUiAction {  // UiAction để xử lý các sự kiện giao diện người dùng
    data object Load : ExpenseUiAction
    data class Add(val expense: ExpenseEntity) : ExpenseUiAction
    data class Update(val expense: ExpenseEntity) : ExpenseUiAction
    data class Delete(val expense: ExpenseEntity) : ExpenseUiAction
    data class Search(val query: String) : ExpenseUiAction
    data class FilterByDateRange(val startDate: Long, val endDate: Long) : ExpenseUiAction
    data class GetPricesByCategory(val category: String) : ExpenseUiAction
    data class GetByDay(val day: Long) : ExpenseUiAction
}

data class ExpenseUiState(  // UiState để quản lý trạng thái giao diện
    val expenses: List<ExpenseEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ExpenseEvent { // Event để xử lý các sự kiện one-time chỉ 1 lần (như toast)
    data class ShowToast(val message: String) : ExpenseEvent()
    data class ShowPrices(val category: String, val prices: List<Double>) : ExpenseEvent()
    data object ExpenseAdded : ExpenseEvent()
    data object ExpenseUpdated : ExpenseEvent()
    data object ExpenseDeleted : ExpenseEvent()
    data object ExpenseSearched : ExpenseEvent()
    data object ExpenseFiltered : ExpenseEvent()
}

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {
    // State
    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    // Event
    private val _eventChannel = Channel<ExpenseEvent>(Channel.BUFFERED)
    val eventFlow = _eventChannel.receiveAsFlow()

    // Actions
    private val _actionSharedFlow = MutableSharedFlow<ExpenseUiAction>()
    fun dispatch(action: ExpenseUiAction): Job {
        return viewModelScope.launch {
            _actionSharedFlow.emit(action)
        }
    }

    init {
        Log.d("__INSTANCE", "Instance ViewModel: ${this.hashCode()}")
        getAndRefreshAllExpenses()
        handleActions()
    }

    private fun handleActions() {
        viewModelScope.launch {
            _actionSharedFlow.collect { action ->
                when (action) {
                    is ExpenseUiAction.Load -> getAndRefreshAllExpenses()
                    is ExpenseUiAction.Add -> addExpense(action.expense)
                    is ExpenseUiAction.Update -> updateExpense(action.expense)
                    is ExpenseUiAction.Delete -> deleteExpense(action.expense)
                    is ExpenseUiAction.Search -> collectFlow(
                        repository.searchExpenses(action.query), "Search failed"
                    ) { _eventChannel.send(ExpenseEvent.ExpenseSearched) }

                    is ExpenseUiAction.FilterByDateRange -> collectFlow(
                        repository.getExpensesByDateRange(action.startDate, action.endDate),
                        "Filter failed"
                    ) { _eventChannel.send(ExpenseEvent.ExpenseFiltered) }

                    is ExpenseUiAction.GetByDay -> collectFlow(
                        repository.getExpensesByDay(action.day), "Get day expenses failed"
                    )

                    is ExpenseUiAction.GetPricesByCategory -> collectPricesFlow(
                        repository.getPricesByCategory(action.category), "Get prices failed"
                    )
                }
            }
        }
    }

    fun getAndRefreshAllExpenses() {
        viewModelScope.launch {
            repository.getAllExpenses().onStart {
                    _uiState.update { it.copy(isLoading = true) }
                }.catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    _eventChannel.send(ExpenseEvent.ShowToast("Load failed: ${e.message}"))
                }.collect { expenses ->
                    _uiState.update {
                        it.copy(
                            expenses = expenses, isLoading = false, error = null
                        )
                    }
                }
        }

    }

    private suspend fun addExpense(expense: ExpenseEntity) {
        try {
            repository.insertExpense(expense)
            _eventChannel.send(ExpenseEvent.ExpenseAdded)
            _eventChannel.send(ExpenseEvent.ShowToast("Add success"))
            getAndRefreshAllExpenses()
        } catch (e: Exception) {
            _eventChannel.send(ExpenseEvent.ShowToast("Add failed: ${e.message}"))
        }
    }

    private suspend fun updateExpense(expense: ExpenseEntity) {
        try {
            repository.updateExpense(expense)
            _eventChannel.send(ExpenseEvent.ExpenseUpdated)
            getAndRefreshAllExpenses()
        } catch (e: Exception) {
            _eventChannel.send(ExpenseEvent.ShowToast("Update failed: ${e.message}"))
        }
    }

    private suspend fun deleteExpense(expense: ExpenseEntity) {
        try {
            repository.deleteExpense(expense)
            _eventChannel.send(ExpenseEvent.ExpenseDeleted)
            getAndRefreshAllExpenses()
        } catch (e: Exception) {
            _eventChannel.send(ExpenseEvent.ShowToast("Delete failed: ${e.message}"))
        }
    }

    private fun collectFlow(
        flow: Flow<List<ExpenseEntity>>, errorMessage: String, onSuccess: suspend () -> Unit = {}
    ) {
        viewModelScope.launch {
            flow.onStart { _uiState.update { it.copy(isLoading = true) } }.catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    _eventChannel.send(ExpenseEvent.ShowToast("$errorMessage: ${e.message}"))
                }.collect { expenses ->
                    _uiState.update {
                        it.copy(
                            expenses = expenses, isLoading = false, error = null
                        )
                    }
                    onSuccess()
                }
        }
    }

    private fun collectPricesFlow(flow: Flow<List<Double>>, errorMessage: String) {
        viewModelScope.launch {
            flow.onStart { _uiState.update { it.copy(isLoading = true) } }.catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    _eventChannel.send(ExpenseEvent.ShowToast("$errorMessage: ${e.message}"))
                }.collect { prices ->
                    _uiState.update { it.copy(isLoading = false, error = null) }
                    _eventChannel.send(ExpenseEvent.ShowPrices("category", prices))
                }
        }
    }
}

/*
data class ExpenseUiState(
    val expenses: List<ExpenseEntity> = emptyList(),
    val isLoading: Boolean = false
)

sealed class ExpenseAction {
    data object Load : ExpenseAction()
    data class Add(val expense: ExpenseEntity) : ExpenseAction()
    data class Update(val expense: ExpenseEntity) : ExpenseAction()
    data class Delete(val expense: ExpenseEntity) : ExpenseAction()
    data class Search(val query: String) : ExpenseAction()
    data class FilterByDateRange(val startDate: Long, val endDate: Long) : ExpenseAction()
    data class GetPricesByCategory(val category: String) : ExpenseAction()
    data class GetByDay(val day: Long) : ExpenseAction()
}

sealed class ExpenseEvent {
    data class AddSuccess(val expense: ExpenseEntity) : ExpenseEvent()
    data class ShowSuccess(val message: String) : ExpenseEvent()
    data class ShowError(val message: String) : ExpenseEvent()
    data class ShowPrices(val category: String, val prices: List<Double>) : ExpenseEvent()
}

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val actionScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableStateFlow<ExpenseEvent?>(null)
    val eventFlow: StateFlow<ExpenseEvent?> = _eventFlow.asStateFlow()

    init {
        Log.d("__INSTANCE", "Instance VM: ${this.hashCode()}")
        loadExpenses()
    }

    fun dispatch(action: ExpenseAction): Job {
        return actionScope.launch {
            Log.i("__VM", "Dispatching action: $action")
            handleAction(action)
        }
    }

    private fun handleAction(action: ExpenseAction) {
        when (action) {
            is ExpenseAction.Load -> loadExpenses()
            is ExpenseAction.Add -> addExpense(action.expense)
            is ExpenseAction.Update -> updateExpense(action.expense)
            is ExpenseAction.Delete -> deleteExpense(action.expense)
            is ExpenseAction.Search -> searchExpense(action.query)
            is ExpenseAction.FilterByDateRange -> filterByDateRange(
                action.startDate,
                action.endDate
            )

            is ExpenseAction.GetPricesByCategory -> getPricesByCategory(action.category)
            is ExpenseAction.GetByDay -> getExpensesByDay(action.day)
        }
    }

    private fun loadExpenses() {
        actionScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getAllExpenses().fold(
                onSuccess = { expenses ->
                    Log.i("__VM", "Loaded ${expenses.size} expenses")
                    _uiState.value = _uiState.value.copy(expenses = expenses, isLoading = false)
                },
                onFailure = { error ->
                    Log.e("__VM", "Failed: ${error.message}")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _eventFlow.value =
                        ExpenseEvent.ShowError("Failed to load expenses: ${error.message}")
                }
            )
        }
    }

    private fun addExpense(expense: ExpenseEntity) {
        actionScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.insertExpense(expense).fold(
                onSuccess = { id ->
                    Log.i("__VM", "Expense inserted with ID: $id")
                    val updatedExpenses = _uiState.value.expenses + expense.copy(id = id.toInt())
                    _uiState.value =
                        _uiState.value.copy(expenses = updatedExpenses, isLoading = false)
                    _eventFlow.value = ExpenseEvent.AddSuccess(expense.copy(id = id.toInt()))
                    _eventFlow.value =
                        ExpenseEvent.ShowSuccess("Expense added successfully id = $id")
                },
                onFailure = { error ->
                    Log.e("__VM", "Failed to add expense: ${error.message}")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _eventFlow.value =
                        ExpenseEvent.ShowError("Failed to add expense: ${error.message}")
                }
            )
        }
    }

    private fun updateExpense(expense: ExpenseEntity) {
        actionScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.updateExpense(expense).fold(
                onSuccess = {
                    Log.i("__VM", "Update Expense: $expense")
                    _eventFlow.value = ExpenseEvent.ShowSuccess("Updated expense")
                    loadExpenses() // Reload để cập nhật danh sách
                },
                onFailure = { error ->
                    Log.e("__VM", "Failed to update expense: ${error.message}")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _eventFlow.value = ExpenseEvent.ShowError("Failed to update: ${error.message}")
                }
            )
        }
    }

    private fun deleteExpense(expense: ExpenseEntity) {
        actionScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.deleteExpense(expense).fold(
                onSuccess = {
                    Log.i("__VM", "Delete Expense: $expense")
                    _eventFlow.value = ExpenseEvent.ShowSuccess("Deleted expense")
                    loadExpenses() // Reload để cập nhật danh sách
                },
                onFailure = { error ->
                    Log.e("__VM", "Failed to delete expense: ${error.message}")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _eventFlow.value = ExpenseEvent.ShowError("Failed to delete: ${error.message}")
                }
            )
        }
    }

    private fun searchExpense(query: String) {
        actionScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            Log.i("__VM", "Handling Search action: $query")
            repository.searchExpenses(query).fold(
                onSuccess = { results ->
                    Log.i("__VM", "Search returned ${results.size} results")
                    _uiState.value = _uiState.value.copy(expenses = results, isLoading = false)
                },
                onFailure = { error ->
                    Log.e("__VM", "Search failed: ${error.message}")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _eventFlow.value = ExpenseEvent.ShowError("Search failed: ${error.message}")
                }
            )
        }
    }

    private fun filterByDateRange(startDate: Long, endDate: Long) {
        actionScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getExpensesByDateRange(startDate, endDate).fold(
                onSuccess = { results ->
                    Log.i("__VM", "Filter returned ${results.size} results")
                    _uiState.value = _uiState.value.copy(expenses = results, isLoading = false)
                },
                onFailure = { error ->
                    Log.e("__VM", "Failed to filter: ${error.message}")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _eventFlow.value = ExpenseEvent.ShowError("Filter failed: ${error.message}")
                }
            )
        }
    }

    private fun getPricesByCategory(category: String) {
        actionScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            Log.i("__VM", "Handling GetPricesByCategory action: $category")
            repository.getPricesByCategory(category).fold(
                onSuccess = { prices ->
                    Log.i("__VM", "Got prices: $prices")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _eventFlow.value = ExpenseEvent.ShowPrices(category, prices)
                },
                onFailure = { error ->
                    Log.e("__VM", "Failed to get prices: ${error.message}")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _eventFlow.value =
                        ExpenseEvent.ShowError("Failed to get prices: ${error.message}")
                }
            )
        }
    }

    private fun getExpensesByDay(day: Long) {
        actionScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            Log.i("__VM", "Handling GetByDay action: $day")
            repository.getExpensesByDay(day).fold(
                onSuccess = { expenses ->
                    Log.i("__VM", "Got ${expenses.size} expenses for day")
                    _uiState.value = _uiState.value.copy(expenses = expenses, isLoading = false)
                    _eventFlow.value = ExpenseEvent.ShowSuccess("Loaded expenses for the day")
                },
                onFailure = { error ->
                    Log.e("__VM", "Failed to load day expenses: ${error.message}")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _eventFlow.value =
                        ExpenseEvent.ShowError("Failed to load day expenses: ${error.message}")
                }
            )
        }
    }

    override fun onCleared() {
        Log.i("__VM", "ViewModel cleared")
        actionScope.cancel()
        super.onCleared()
    }
}


*/