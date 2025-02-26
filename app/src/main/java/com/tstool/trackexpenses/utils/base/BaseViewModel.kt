package com.tstool.trackexpenses.utils.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<UI_STATE, ACTION, EVENT> : ViewModel() {
    abstract fun initUiState(): UI_STATE

    /**
     * UI_STATE – Trạng thái UI
     *
     * 🔹 Chức năng:
     *
     * + mutableStateFlow: giữ trạng thái UI (UI_STATE) và phát tín hiệu khi có thay đổi.
     * + uiStateFlow: là StateFlow giúp UI lắng nghe liên tục trạng thái mới.
     * + uiState: cung cấp giá trị hiện tại của trạng thái mà không cần lắng nghe.
     */
    protected val mutableStateFlow = MutableStateFlow(initUiState())
    val uiStateFlow: StateFlow<UI_STATE> = mutableStateFlow
    val uiState: UI_STATE
        get() = uiStateFlow.value


    /**
     * ACTION – Hành động từ UI
     *
     * 🔹 Chức năng:
     *
     * + actionSharedFlow: Dùng SharedFlow để phát hành động từ UI (bấm nút, nhập dữ liệu, chọn item...).
     * + actionFlow<T>(): Bộ lọc cho từng loại hành động cụ thể.
     * + dispatch(action): Gửi hành động vào SharedFlow, giúp UI gửi sự kiện đến ViewModel.
     */
    val actionSharedFlow = MutableSharedFlow<ACTION>(extraBufferCapacity = 10) // Buffer actions to prevent loss if no collector is active
    inline fun <reified T : ACTION> actionFlow() = actionSharedFlow.filterIsInstance<T>()
    open fun dispatch(action: ACTION): Job = viewModelScope.launch {
        actionSharedFlow.emit(action)
    }

    /**
     * EVENT – Sự kiện một lần (One-shot Event)
     * + Gửi các sự kiện. Các sự kiện này có thể được lắng nghe thông qua eventFlow
     *
     * 🔹 Chức năng:
     * + eventChannel lưu trữ các sự kiện một lần như Snackbar, Toast, Navigation.
     * + eventFlow chuyển đổi Channel thành Flow, giúp UI lắng nghe sự kiện mới.
     * + sendEvent(event) gửi sự kiện vào Channel.
     */
    private val eventChannel = Channel<EVENT>(Channel.UNLIMITED)
    val eventFlow = eventChannel.receiveAsFlow()

    fun sendEvent(event: EVENT) = viewModelScope.launch {
        eventChannel.send(event)
    }

    override fun onCleared() {
        super.onCleared()
        eventChannel.close()
    }
}