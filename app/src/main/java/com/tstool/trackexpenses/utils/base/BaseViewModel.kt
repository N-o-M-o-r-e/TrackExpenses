package com.tstool.trackexpenses.utils.base

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<UI_STATE, ACTION, EVENT> : ViewModel() {
    abstract fun initUiState(): UI_STATE
    protected val mutableStateFlow = MutableStateFlow(initUiState())
    val uiStateFlow: StateFlow<UI_STATE> = mutableStateFlow
    val uiState: UI_STATE get() = uiStateFlow.value

    val actionSharedFlow = MutableSharedFlow<ACTION>()
    inline fun <reified T : ACTION> actionFlow() = actionSharedFlow.filterIsInstance<T>()
    open fun dispatch(action: ACTION): Job = viewModelScope.launch {
        Log.i("__VM", "Dispatching action: $action")
        actionSharedFlow.emit(action)
    }

    private val eventChannel = Channel<EVENT>(Channel.UNLIMITED)
    val eventFlow = eventChannel.receiveAsFlow()
    fun sendEvent(event: EVENT) = viewModelScope.launch {
        eventChannel.send(event)
    }

    override fun onCleared() {
        Log.i("__VM", "ViewModel cleared")
        eventChannel.close()
        super.onCleared()
    }
}