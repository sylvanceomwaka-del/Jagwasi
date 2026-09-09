package com.jagwasi.presentation.governance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jagwasi.data.local.entity.AuditEntry
import com.jagwasi.data.repository.AuditEntryRepository
import com.jagwasi.data.repository.OrderRepository
import com.jagwasi.data.repository.PaymentRepository
import com.jagwasi.data.repository.StaffRepository
import com.jagwasi.data.repository.StockItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * UI state for the Supervisor dashboard.
 */
sealed class SupervisorUiState {
    object Loading : SupervisorUiState()
    data class FraudAlerts(val alerts: List<String>) : SupervisorUiState()
    data class PendingApprovals(val approvals: List<Pair<String, Any>>) : SupervisorUiState()
    data class EndOfShiftReport(val report: String) : SupervisorUiState()
    data class Error(val message: String) : SupervisorUiState()
}

/**
 * ViewModel for supervisor-level operations:
 * fraud detection, approvals, end‑of‑shift reporting.
 */
class SupervisorViewModel(
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
    private val auditRepository: AuditEntryRepository,
    private val staffRepository: StaffRepository,      // Added
    private val stockRepository: StockItemRepository  // Added for stock summary
) : ViewModel() {

    private val _uiState = MutableStateFlow<SupervisorUiState>(SupervisorUiState.Loading)
    val uiState: StateFlow<SupervisorUiState> = _uiState.asStateFlow()

    /**
     * Load fraud alerts by analyzing audit logs for suspicious patterns.
     * Example: >3 voids or discounts in 10 minutes by the same staff.
     */
    fun loadFraudAlerts() {
        viewModelScope.launch {
            _uiState.value = SupervisorUiState.Loading
            try {
                val now = System.currentTimeMillis()
                val oneHourAgo = now - 60 * 60 * 1000
                val entries = auditRepository.getAuditEntriesBetween(oneHourAgo, now).first()

                val alerts = mutableListOf<String>()
                val actorActions = entries.groupBy { it.actor }
                actorActions.forEach { (actor, actions) ->
                    val voids = actions.filter { it.action.contains("VOID") }
                    val discounts = actions.filter { it.action.contains("DISCOUNT") }
                    if (voids.size > 3) {
                        alerts.add("$actor performed ${voids.size} voids in the last hour.")
                    }
                    if (discounts.size > 3) {
                        alerts.add("$actor applied ${discounts.size} discounts in the last hour.")
                    }
                }

                if (alerts.isEmpty()) {
                    alerts.add("No suspicious activity detected.")
                }

                _uiState.value = SupervisorUiState.FraudAlerts(alerts)
            } catch (e: Exception) {
                _uiState.value = SupervisorUiState.Error(e.message ?: "Failed to load fraud alerts")
            }
        }
    }

    /**
     * Load pending approvals – actions that require supervisor authorization.
     */
    fun loadPendingApprovals() {
        viewModelScope.launch {
            _uiState.value = SupervisorUiState.Loading
            try {
                // In a real system, this would query a pending approvals table.
                // For demonstration, we use dummy data.
                val pending = listOf(
                    "Order #123 - Discount >50%" to "Order",
                    "Order #124 - Void request" to "Void",
                    "Payment #456 - Refund > $50" to "Refund"
                )
                _uiState.value = SupervisorUiState.PendingApprovals(pending)
            } catch (e: Exception) {
                _uiState.value = SupervisorUiState.Error(e.message ?: "Failed to load pending approvals")
            }
        }
    }

    /**
     * Approve an action by its ID.
     * @param actionId Identifier for the action.
     */
    fun approveAction(actionId: String) {
        viewModelScope.launch {
            try {
                val audit = AuditEntry(
                    actor = "SUPERVISOR",
                    action = "ACTION_APPROVED",
                    details = "Approved action: $actionId",
                    timestamp = System.currentTimeMillis()
                )
                auditRepository.insertAuditEntry(audit)
                loadPendingApprovals()
            } catch (e: Exception) {
                _uiState.value = SupervisorUiState.Error(e.message ?: "Failed to approve action")
            }
        }
    }

    /**
     * Reject an action with a reason.
     * @param actionId Identifier for the action.
     * @param reason Reason for rejection.
     */
    fun rejectAction(actionId: String, reason: String) {
        viewModelScope.launch {
            try {
                val audit = AuditEntry(
                    actor = "SUPERVISOR",
                    action = "ACTION_REJECTED",
                    details = "Rejected action: $actionId. Reason: $reason",
                    timestamp = System.currentTimeMillis()
                )
                auditRepository.insertAuditEntry(audit)
                loadPendingApprovals()
            } catch (e: Exception) {
                _uiState.value = SupervisorUiState.Error(e.message ?: "Failed to reject action")
            }
        }
    }

    /**
     * Generate an end‑of‑shift report summarizing revenue, orders,
     * stock usage, and staff performance.
     * @return A formatted report string (plain text).
     */
    fun generateEndOfShiftReport() {
        viewModelScope.launch {
            _uiState.value = SupervisorUiState.Loading
            try {
                val shiftStart = System.currentTimeMillis() - 8 * 60 * 60 * 1000
                val shiftEnd = System.currentTimeMillis()

                // Orders
                val allOrders = orderRepository.getAllOrders().first()
                val shiftOrders = allOrders.filter { it.timestamp in shiftStart..shiftEnd }
                val totalRevenue = shiftOrders.sumOf { it.totalAmount }
                val orderCount = shiftOrders.size

                // Payments
                val allPayments = paymentRepository.getAllPayments().first()
                val shiftPayments = allPayments.filter { it.timestamp in shiftStart..shiftEnd }
                val cashPayments = shiftPayments.filter { it.method == "CASH" }.sumOf { it.amount }
                val mpesaPayments = shiftPayments.filter { it.method == "M-PESA" }.sumOf { it.amount }
                val cardPayments = shiftPayments.filter { it.method == "CARD" }.sumOf { it.amount }

                // Stock usage – get all stock items and compute total usage from audit logs (STOCK_DEDUCTED)
                val allStockItems = stockRepository.getAllStockItems().first()
                val stockAudits = auditRepository.getAllAuditEntries().first()
                    .filter { it.action == "STOCK_DEDUCTED" && it.timestamp in shiftStart..shiftEnd }
                val stockUsage = stockAudits.groupBy { it.details }
                    .mapValues { it.value.size } // simplistic; real would parse quantities

                // Staff performance – count orders per staff (from audit logs)
                val auditEntries = auditRepository.getAllAuditEntries().first()
                val staffOrders = auditEntries
                    .filter { it.action == "ORDER_CREATED" && it.timestamp in shiftStart..shiftEnd }
                    .groupBy { it.actor }
                    .mapValues { it.value.size }

                // Build report
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val sb = StringBuilder()
                sb.appendLine("=== END OF SHIFT REPORT ===")
                sb.appendLine("Shift period: ${dateFormat.format(shiftStart)} - ${dateFormat.format(shiftEnd)}")
                sb.appendLine("Orders: $orderCount")
                sb.appendLine("Total Revenue: $${String.format("%.2f", totalRevenue)}")
                sb.appendLine("Payment Breakdown:")
                sb.appendLine("  Cash: $${String.format("%.2f", cashPayments)}")
                sb.appendLine("  M-PESA: $${String.format("%.2f", mpesaPayments)}")
                sb.appendLine("  Card: $${String.format("%.2f", cardPayments)}")
                sb.appendLine("Staff Order Performance:")
                if (staffOrders.isEmpty()) {
                    sb.appendLine("  No orders processed.")
                } else {
                    staffOrders.forEach { (staff, count) ->
                        sb.appendLine("  $staff: $count orders")
                    }
                }
                sb.appendLine("Stock Usage Summary:")
                if (stockUsage.isEmpty()) {
                    sb.appendLine("  No stock movements recorded.")
                } else {
                    stockUsage.forEach { (detail, count) ->
                        sb.appendLine("  $detail: $count occurrences")
                    }
                }
                sb.appendLine("============================")

                _uiState.value = SupervisorUiState.EndOfShiftReport(sb.toString())
            } catch (e: Exception) {
                _uiState.value = SupervisorUiState.Error(e.message ?: "Failed to generate report")
            }
        }
    }

    /**
     * Export the current report in a specific format.
     * @param format "PDF", "HTML", or "CSV"
     */
    fun exportReport(format: String) {
        viewModelScope.launch {
            try {
                val audit = AuditEntry(
                    actor = "SUPERVISOR",
                    action = "REPORT_EXPORT",
                    details = "Exported end-of-shift report in $format format.",
                    timestamp = System.currentTimeMillis()
                )
                auditRepository.insertAuditEntry(audit)
            } catch (e: Exception) {
                _uiState.value = SupervisorUiState.Error(e.message ?: "Failed to export report")
            }
        }
    }

    /**
     * Clear error state and return to loading.
     */
    fun clearError() {
        val current = _uiState.value
        if (current is SupervisorUiState.Error) {
            _uiState.value = SupervisorUiState.Loading
        }
    }
}
