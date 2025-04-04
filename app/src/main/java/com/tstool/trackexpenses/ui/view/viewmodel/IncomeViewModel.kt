package com.tstool.trackexpenses.ui.view.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tstool.trackexpenses.data.room.repository.IncomeRepository
import com.tstool.trackexpenses.data.room.entity.IncomeEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

sealed interface IncomeUiAction {
    data object Load : IncomeUiAction
    data class Add(val income: IncomeEntity) : IncomeUiAction
    data class Update(val income: IncomeEntity) : IncomeUiAction
    data class Delete(val income: IncomeEntity) : IncomeUiAction
    data class Search(val category: String) : IncomeUiAction
    data class FilterByDateRange(val startDate: Long, val endDate: Long) : IncomeUiAction
    data class GetAmountsByCategory(val category: String) : IncomeUiAction
    data class GetByDay(val day: Long) : IncomeUiAction
}

data class IncomeUiState(
    val incomes: List<IncomeEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val startDate: Long? = null,
    val endDate: Long? = null
)

sealed class IncomeEvent {
    data class ShowToast(val message: String) : IncomeEvent()
    data class ShowAmounts(val category: String, val amounts: List<Double>) : IncomeEvent()
    data object IncomeAdded : IncomeEvent()
    data object IncomeUpdated : IncomeEvent()
    data object IncomeDeleted : IncomeEvent()
    data object IncomeSearched : IncomeEvent()
    data object IncomeFiltered : IncomeEvent()
}

class IncomeViewModel(private val repository: IncomeRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(IncomeUiState())
    val uiState: StateFlow<IncomeUiState> = _uiState.asStateFlow()

    private val _eventChannel = Channel<IncomeEvent>(Channel.BUFFERED)
    val eventFlow = _eventChannel.receiveAsFlow()

    private val _actionSharedFlow = MutableSharedFlow<IncomeUiAction>()
    fun dispatch(action: IncomeUiAction): Job = viewModelScope.launch {
        _actionSharedFlow.emit(action)
    }

    init {
        Log.d("__INSTANCE", "Instance ViewModel: ${this.hashCode()}")
        // Khởi tạo với tháng hiện tại
        val calendar = Calendar.getInstance()
        val startOfMonth = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfMonth = calendar.apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        // Tải dữ liệu trực tiếp thay vì dispatch
        _uiState.update { it.copy(startDate = startOfMonth, endDate = endOfMonth) }
        collectFlow(
            repository.getIncomeByDateRange(startOfMonth, endOfMonth),
            "Initial load failed"
        ) { _eventChannel.trySend(IncomeEvent.IncomeFiltered) }

        handleActions()
    }

    private fun handleActions() {
        viewModelScope.launch {
            _actionSharedFlow.collect { action ->
                when (action) {
                    is IncomeUiAction.Load -> getAndRefreshAllIncome()
                    is IncomeUiAction.Add -> addIncome(action.income)
                    is IncomeUiAction.Update -> updateIncome(action.income)
                    is IncomeUiAction.Delete -> deleteIncome(action.income)
                    is IncomeUiAction.Search -> {
                        val currentState = _uiState.value
                        if (currentState.startDate != null && currentState.endDate != null) {
                            collectFlow(
                                repository.getIncomeByCategoryAndDateRange(
                                    action.category,
                                    currentState.startDate,
                                    currentState.endDate
                                ),
                                "Search failed"
                            ) { _eventChannel.send(IncomeEvent.IncomeSearched) }
                        } else {
                            collectFlow(
                                repository.searchIncome(action.category),
                                "Search failed"
                            ) { _eventChannel.send(IncomeEvent.IncomeSearched) }
                        }
                    }
                    is IncomeUiAction.FilterByDateRange -> {
                        _uiState.update { it.copy(startDate = action.startDate, endDate = action.endDate) }
                        collectFlow(
                            repository.getIncomeByDateRange(action.startDate, action.endDate),
                            "Filter failed"
                        ) { _eventChannel.send(IncomeEvent.IncomeFiltered) }
                    }
                    is IncomeUiAction.GetByDay -> collectFlow(
                        repository.getIncomeByDay(action.day),
                        "Get day income failed"
                    )
                    is IncomeUiAction.GetAmountsByCategory -> collectAmountsFlow(
                        repository.getAmountsByCategory(action.category),
                        "Get amounts failed"
                    )
                }
            }
        }
    }

    private fun getAndRefreshAllIncome() {
        viewModelScope.launch {
            repository.getAllIncome()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    _eventChannel.send(IncomeEvent.ShowToast("Load failed: ${e.message}"))
                }
                .collect { incomes ->
                    _uiState.update { it.copy(incomes = incomes, isLoading = false, error = null) }
                }
        }
    }

    private suspend fun addIncome(income: IncomeEntity) {
        try {
            repository.insertIncome(income)
            _eventChannel.send(IncomeEvent.IncomeAdded)
            _eventChannel.send(IncomeEvent.ShowToast("Add success"))
            val currentState = _uiState.value
            if (currentState.startDate != null && currentState.endDate != null) {
                dispatch(IncomeUiAction.FilterByDateRange(currentState.startDate, currentState.endDate))
            } else {
                getAndRefreshAllIncome()
            }
        } catch (e: Exception) {
            _eventChannel.send(IncomeEvent.ShowToast("Add failed: ${e.message}"))
        }
    }

    private suspend fun updateIncome(income: IncomeEntity) {
        try {
            repository.updateIncome(income)
            _eventChannel.send(IncomeEvent.IncomeUpdated)
            val currentState = _uiState.value
            if (currentState.startDate != null && currentState.endDate != null) {
                dispatch(IncomeUiAction.FilterByDateRange(currentState.startDate, currentState.endDate))
            } else {
                getAndRefreshAllIncome()
            }
        } catch (e: Exception) {
            _eventChannel.send(IncomeEvent.ShowToast("Update failed: ${e.message}"))
        }
    }

    private suspend fun deleteIncome(income: IncomeEntity) {
        try {
            repository.deleteIncome(income)
            _eventChannel.send(IncomeEvent.IncomeDeleted)
            val currentState = _uiState.value
            if (currentState.startDate != null && currentState.endDate != null) {
                dispatch(IncomeUiAction.FilterByDateRange(currentState.startDate, currentState.endDate))
            } else {
                getAndRefreshAllIncome()
            }
        } catch (e: Exception) {
            _eventChannel.send(IncomeEvent.ShowToast("Delete failed: ${e.message}"))
        }
    }

    private fun collectFlow(
        flow: Flow<List<IncomeEntity>>,
        errorMessage: String,
        onSuccess: suspend () -> Unit = {}
    ) {
        viewModelScope.launch {
            flow.onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    _eventChannel.send(IncomeEvent.ShowToast("$errorMessage: ${e.message}"))
                }
                .collect { incomes ->
                    _uiState.update { it.copy(incomes = incomes, isLoading = false, error = null) }
                    onSuccess()
                }
        }
    }

    private fun collectAmountsFlow(flow: Flow<List<Double>>, errorMessage: String) {
        viewModelScope.launch {
            flow.onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    _eventChannel.send(IncomeEvent.ShowToast("$errorMessage: ${e.message}"))
                }
                .collect { amounts ->
                    _uiState.update { it.copy(isLoading = false, error = null) }
                    _eventChannel.send(IncomeEvent.ShowAmounts("category", amounts))
                }
        }
    }
}