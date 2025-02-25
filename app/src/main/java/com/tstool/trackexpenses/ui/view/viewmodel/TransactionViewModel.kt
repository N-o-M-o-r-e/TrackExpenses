package com.tstool.trackexpenses.ui.view.viewmodel

import androidx.lifecycle.viewModelScope
import com.tstool.trackexpenses.data.room.entity.TransactionEntity
import com.tstool.trackexpenses.data.room.repository.TransactionRepository
import com.tstool.trackexpenses.utils.base.BaseViewModel
import kotlinx.coroutines.launch

class TransactionViewModel(private val repository: TransactionRepository) :
    BaseViewModel<TransactionUiState, TransactionAction, TransactionEvent>() {

    override fun initUiState(): TransactionUiState = TransactionUiState()

    init {
        viewModelScope.launch {
            loadTransactions()
            handleActions()
        }
    }

    private suspend fun loadTransactions() {
        val transactions = repository.getAllTransactions()
        mutableStateFlow.value = uiState.copy(transactions = transactions)
    }

    private suspend fun handleActions() {
        actionSharedFlow.collect { action ->
            when (action) {
                is TransactionAction.Add -> {
                    val id = repository.insertTransaction(action.transaction)
                    if (id > 0) {
                        loadTransactions()
                        sendEvent(TransactionEvent.ShowSuccess("Added transaction"))
                    }
                }
                is TransactionAction.Update -> {
                    repository.updateTransaction(action.transaction)
                    loadTransactions()
                    sendEvent(TransactionEvent.ShowSuccess("Updated transaction"))
                }
                is TransactionAction.Delete -> {
                    repository.deleteTransaction(action.transaction)
                    loadTransactions()
                    sendEvent(TransactionEvent.ShowSuccess("Deleted transaction"))
                }
                is TransactionAction.Search -> {
                    val results = repository.searchTransactions(action.query)
                    mutableStateFlow.value = uiState.copy(transactions = results)
                }
            }
        }
    }
}

data class TransactionUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = false
)

sealed class TransactionAction {
    data class Add(val transaction: TransactionEntity) : TransactionAction()
    data class Update(val transaction: TransactionEntity) : TransactionAction()
    data class Delete(val transaction: TransactionEntity) : TransactionAction()
    data class Search(val query: String) : TransactionAction()
}

sealed class TransactionEvent {
    data class ShowSuccess(val message: String) : TransactionEvent()
}