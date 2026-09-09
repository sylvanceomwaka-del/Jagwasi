package com.jagwasi.presentation.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jagwasi.data.local.entity.Payment
import com.jagwasi.domain.usecase.DetectUnpaidExitUseCase
import com.jagwasi.domain.usecase.GenerateReceiptUseCase
import com.jagwasi.domain.usecase.ProcessPaymentUseCase
import com.jagwasi.domain.usecase.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Persistent UI state for the Payment screen.
 */
data class PaymentUiState(
    val orderId: Int? = null,
    val payment: Payment? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * One-time transient events for UI navigation, dialogs, and alerts.
 */
sealed class PaymentUiEvent {
    data class PaymentSuccess(val payment: Payment) : PaymentUiEvent()
    data class ShowReceipt(val receiptText: String) : PaymentUiEvent()
    data class ShowError(val message: String) : PaymentUiEvent()
    object UnpaidExitDetected : PaymentUiEvent()
}

class PaymentViewModel(
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val generateReceiptUseCase: GenerateReceiptUseCase,
    private val detectUnpaidExitUseCase: DetectUnpaidExitUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<PaymentUiEvent>()
    val eventFlow: SharedFlow<PaymentUiEvent> = _eventFlow.asSharedFlow()

    fun initialize(orderId: Int) {
        _uiState.update { it.copy(orderId = orderId) }
    }

    fun processPayment(amount: Double, method: String) {
        val orderId = _uiState.value.orderId ?: run {
            emitError("Order ID not set")
            return
        }

        viewModelScope.launch {
            setLoading()
            val payment = Payment(
                orderId = orderId,
                amount = amount,
                method = method,
                status = "PENDING",
                timestamp = System.currentTimeMillis()
            )

            when (val result = processPaymentUseCase(payment)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(payment = result.data, isLoading = false, errorMessage = null)
                    }
                    _eventFlow.emit(PaymentUiEvent.PaymentSuccess(result.data))
                }
                is Result.Failure -> handleError(result.message)
            }
        }
    }

    fun generateReceipt() {
        val orderId = _uiState.value.orderId ?: run {
            emitError("Order ID not set")
            return
        }

        viewModelScope.launch {
            setLoading()
            when (val result = generateReceiptUseCase(orderId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                    _eventFlow.emit(PaymentUiEvent.ShowReceipt(result.data))
                }
                is Result.Failure -> handleError(result.message)
            }
        }
    }

    fun detectUnpaidExit() {
        val orderId = _uiState.value.orderId ?: run {
            emitError("Order ID not set")
            return
        }

        viewModelScope.launch {
            setLoading()
            when (val result = detectUnpaidExitUseCase(orderId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                    if (result.data) {
                        _eventFlow.emit(PaymentUiEvent.UnpaidExitDetected)
                    }
                }
                is Result.Failure -> handleError(result.message)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun setLoading() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
    }

    private fun handleError(message: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
        viewModelScope.launch { _eventFlow.emit(PaymentUiEvent.ShowError(message)) }
    }

    private fun emitError(message: String) {
        handleError(message)
    }
}

