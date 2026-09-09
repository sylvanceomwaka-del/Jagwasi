package com.jagwasi.presentation.stock
import com.jagwasi.domain.usecase.Result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jagwasi.data.local.entity.StockItem
import com.jagwasi.data.repository.StockItemRepository
import com.jagwasi.domain.usecase.ApplyStockDeductionUseCase
import com.jagwasi.domain.usecase.LogWasteUseCase
import com.jagwasi.domain.usecase.RestockUseCase
import com.jagwasi.domain.usecase.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the Stock screen.
 * Retains list state during operations.
 */
data class StockUiState(
    val items: List<StockItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * One-time events for UI notifications.
 */
sealed class StockUiEvent {
    data class ShowMessage(val message: String) : StockUiEvent()
    data class ShowError(val message: String) : StockUiEvent()
}

class StockViewModel(
    private val stockRepository: StockItemRepository,
    private val applyStockDeductionUseCase: ApplyStockDeductionUseCase,
    private val restockUseCase: RestockUseCase,
    private val logWasteUseCase: LogWasteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockUiState())
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<StockUiEvent>()
    val eventFlow: SharedFlow<StockUiEvent> = _eventFlow.asSharedFlow()

    init {
        observeStockItems()
    }

    private fun observeStockItems() {
        viewModelScope.launch {
            stockRepository.getAllStockItems()
                .catch { e ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Failed to load stock items"
                        )
                    }
                }
                .collect { items ->
                    _uiState.update { current ->
                        current.copy(
                            items = items,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    fun deductStock(itemId: Int, quantity: Double) {
        viewModelScope.launch {
            setLoading()
            when (val result = applyStockDeductionUseCase(listOf(itemId to quantity))) {
                is Result.Success -> handleSuccess("Stock deducted successfully")
                is Result.Failure -> handleError(result.message)
            }
        }
    }

    fun restock(itemId: Int, quantity: Double) {
        viewModelScope.launch {
            setLoading()
            when (val result = restockUseCase(itemId, quantity)) {
                is Result.Success -> handleSuccess("Item restocked successfully")
                is Result.Failure -> handleError(result.message)
            }
        }
    }

    fun logWaste(itemId: Int, quantity: Double, reason: String) {
        viewModelScope.launch {
            setLoading()
            when (val result = logWasteUseCase(itemId, quantity, reason)) {
                is Result.Success -> handleSuccess("Waste logged successfully")
                is Result.Failure -> handleError(result.message)
            }
        }
    }

    fun clearError() {
        _uiState.update { current -> current.copy(errorMessage = null) }
    }

    private fun setLoading() {
        _uiState.update { current -> current.copy(isLoading = true, errorMessage = null) }
    }

    private suspend fun handleSuccess(message: String) {
        _uiState.update { current -> current.copy(isLoading = false, errorMessage = null) }
        _eventFlow.emit(StockUiEvent.ShowMessage(message))
    }

    private suspend fun handleError(message: String) {
        _uiState.update { current -> current.copy(isLoading = false, errorMessage = message) }
        _eventFlow.emit(StockUiEvent.ShowError(message))
    }
}

