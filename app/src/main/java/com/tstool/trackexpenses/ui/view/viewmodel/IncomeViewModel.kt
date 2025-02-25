package com.tstool.trackexpenses.ui.view.viewmodel

import androidx.lifecycle.viewModelScope
import com.tstool.trackexpenses.data.room.entity.IncomeEntity
import com.tstool.trackexpenses.data.room.repository.IncomeRepository
import com.tstool.trackexpenses.utils.base.BaseViewModel
import kotlinx.coroutines.launch

class IncomeViewModel(private val repository: IncomeRepository) :
    BaseViewModel<IncomeUiState, IncomeAction, IncomeEvent>() {

    override fun initUiState(): IncomeUiState = IncomeUiState()

    init {
        viewModelScope.launch {
            loadIncomes()
            handleActions()
        }
    }

    private suspend fun loadIncomes() {
        val incomes = repository.getAllIncomes()
        mutableStateFlow.value = uiState.copy(incomes = incomes)
    }

    private suspend fun handleActions() {
        actionSharedFlow.collect { action ->
            when (action) {
                is IncomeAction.Add -> {
                    val id = repository.insertIncome(action.income)
                    if (id > 0) {
                        loadIncomes()
                        sendEvent(IncomeEvent.ShowSuccess("Added income"))
                    }
                }
                is IncomeAction.Update -> {
                    repository.updateIncome(action.income)
                    loadIncomes()
                    sendEvent(IncomeEvent.ShowSuccess("Updated income"))
                }
                is IncomeAction.Delete -> {
                    repository.deleteIncome(action.income)
                    loadIncomes()
                    sendEvent(IncomeEvent.ShowSuccess("Deleted income"))
                }
                is IncomeAction.Search -> {
                    val results = repository.searchIncomes(action.query)
                    mutableStateFlow.value = uiState.copy(incomes = results)
                }
            }
        }
    }
}

data class IncomeUiState(
    val incomes: List<IncomeEntity> = emptyList(),
    val isLoading: Boolean = false
)

sealed class IncomeAction {
    data class Add(val income: IncomeEntity) : IncomeAction()
    data class Update(val income: IncomeEntity) : IncomeAction()
    data class Delete(val income: IncomeEntity) : IncomeAction()
    data class Search(val query: String) : IncomeAction()
}

sealed class IncomeEvent {
    data class ShowSuccess(val message: String) : IncomeEvent()
}