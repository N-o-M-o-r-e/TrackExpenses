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

    private fun getAndRefreshAllExpenses() {
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
            flow.onStart {
                _uiState.update { it.copy(isLoading = true) }
            }.catch { e ->
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
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
            flow.onStart {
                _uiState.update {
                    it.copy(isLoading = true)
                }
            }.catch { e ->
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
                _eventChannel.send(ExpenseEvent.ShowToast("$errorMessage: ${e.message}"))
            }.collect { prices ->
                _uiState.update { it.copy(isLoading = false, error = null) }
                _eventChannel.send(ExpenseEvent.ShowPrices("category", prices))
            }
        }
    }
}