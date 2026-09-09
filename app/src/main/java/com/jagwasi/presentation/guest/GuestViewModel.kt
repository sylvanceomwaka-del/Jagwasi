package com.jagwasi.presentation.guest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jagwasi.data.local.entity.AuditEntry
import com.jagwasi.data.local.entity.MealCard
import com.jagwasi.data.local.entity.Order
import com.jagwasi.data.repository.AuditEntryRepository
import com.jagwasi.data.repository.MealCardRepository
import com.jagwasi.data.repository.OrderRepository
import com.jagwasi.domain.usecase.CreateOrderUseCase
import com.jagwasi.domain.usecase.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the Guest order screen.
 */
sealed class GuestUiState {
    object Idle : GuestUiState()
    object Loading : GuestUiState()
    data class MenuLoaded(val items: List<MealCard>) : GuestUiState()
    data class OrderPlaced(val orderId: Int) : GuestUiState()
    data class Error(val message: String) : GuestUiState()
    object ConsentRequired : GuestUiState()
}

class GuestViewModel(
    private val orderRepository: OrderRepository,
    private val mealCardRepository: MealCardRepository,
    private val auditRepository: AuditEntryRepository,
    private val createOrderUseCase: CreateOrderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GuestUiState>(GuestUiState.Idle)
    val uiState: StateFlow<GuestUiState> = _uiState.asStateFlow()

    // Selected items: (itemId, quantity)
    private val _selectedItems = MutableStateFlow<List<Pair<Int, Int>>>(emptyList())
    val selectedItems: StateFlow<List<Pair<Int, Int>>> = _selectedItems.asStateFlow()

    /**
     * Load the menu (placeholder – fetch all MealCards as a demo).
     * In production, you would use a dedicated menu repository.
     */
    fun loadMenu() {
        viewModelScope.launch {
            _uiState.value = GuestUiState.Loading
            try {
                val items = mealCardRepository.getAllMealCards().first()
                _uiState.value = GuestUiState.MenuLoaded(items)
            } catch (e: Exception) {
                _uiState.value = GuestUiState.Error(e.message ?: "Failed to load menu")
            }
        }
    }

    fun selectItem(itemId: Int, quantity: Int) {
        if (quantity <= 0) {
            removeItem(itemId)
            return
        }
        _selectedItems.update { current ->
            val existing = current.find { it.first == itemId }
            if (existing != null) {
                current.map { if (it.first == itemId) itemId to quantity else it }
            } else {
                current + (itemId to quantity)
            }
        }
    }

    fun removeItem(itemId: Int) {
        _selectedItems.update { current ->
            current.filter { it.first != itemId }
        }
    }

    fun placeOrder(customerName: String, items: List<Pair<Int, Int>>) {
        if (customerName.isBlank()) {
            _uiState.value = GuestUiState.Error("Customer name cannot be empty")
            return
        }
        if (items.isEmpty()) {
            _uiState.value = GuestUiState.Error("Cannot place an empty order")
            return
        }

        viewModelScope.launch {
            _uiState.value = GuestUiState.Loading

            // Convert Int quantities to Double for the use case
            val itemsWithDouble = items.map { it.first to it.second.toDouble() }

            val order = Order(
                mealCardId = 0, // temporary, will be set during order creation
                customerName = customerName,
                items = items.joinToString(",") { "${it.first}:${it.second}" },
                totalAmount = 0.0, // placeholder – total computed elsewhere
                status = "PENDING",
                timestamp = System.currentTimeMillis()
            )

            when (val result = createOrderUseCase(order, itemsWithDouble)) {
                is Result.Success -> {
                    val insertedOrder = result.data
                    _uiState.value = GuestUiState.OrderPlaced(insertedOrder.id)

                    // Log audit entry
                    val auditEntry = AuditEntry(
                        actor = "GUEST",
                        action = "GUEST_ORDER_PLACED",
                        details = "Guest order placed for $customerName (Order #${insertedOrder.id})",
                        timestamp = System.currentTimeMillis()
                    )
                    auditRepository.insertAuditEntry(auditEntry)

                    // Clear selected items after order placement
                    _selectedItems.value = emptyList()
                }
                is Result.Failure -> {
                    _uiState.value = GuestUiState.Error(result.message)
                }
            }
        }
    }

    fun consentToPortrait() {
        // Log consent and transition to Idle
        viewModelScope.launch {
            val auditEntry = AuditEntry(
                actor = "GUEST",
                action = "PORTRAIT_CONSENT",
                details = "Guest consented to portrait capture",
                timestamp = System.currentTimeMillis()
            )
            auditRepository.insertAuditEntry(auditEntry)
            if (_uiState.value is GuestUiState.ConsentRequired) {
                _uiState.value = GuestUiState.Idle
            }
        }
    }

    fun declinePortrait() {
        viewModelScope.launch {
            val auditEntry = AuditEntry(
                actor = "GUEST",
                action = "PORTRAIT_DECLINE",
                details = "Guest declined portrait capture",
                timestamp = System.currentTimeMillis()
            )
            auditRepository.insertAuditEntry(auditEntry)
            if (_uiState.value is GuestUiState.ConsentRequired) {
                _uiState.value = GuestUiState.Idle
            }
        }
    }

    fun clearError() {
        if (_uiState.value is GuestUiState.Error) {
            _uiState.value = GuestUiState.Idle
        }
    }
}

