package com.jagwasi.domain.usecase

import com.jagwasi.data.local.entity.AuditEntry
import com.jagwasi.data.repository.AuditEntryRepository
import com.jagwasi.data.repository.OrderRepository
import com.jagwasi.data.repository.PaymentRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Use case for generating a receipt for a given order.
 *
 * @param orderRepository Repository for Order entities
 * @param paymentRepository Repository for Payment entities
 * @param auditRepository Repository for AuditEntry entities
 */
class GenerateReceiptUseCase(
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
    private val auditRepository: AuditEntryRepository
) {
    suspend operator fun invoke(orderId: Int): Result<String> {
        return try {
            // 1. Fetch the order
            val order = orderRepository.getOrderById(orderId)
                ?: return Result.Failure("Order with ID $orderId not found")

            // 2. Fetch payments for the order
            val payments = paymentRepository.getPaymentsByOrderId(orderId).first()

            // 3. Generate receipt
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val receipt = buildString {
                appendLine("=== RECEIPT ===")
                appendLine("Order ID: ${order.id}")
                appendLine("Date: ${dateFormat.format(order.timestamp)}")
                appendLine("Customer: ${order.customerName}")
                appendLine("Items: ${order.items}")
                appendLine("Total Amount: ${order.totalAmount}")
                appendLine("Status: ${order.status}")
                appendLine("Payments:")
                if (payments.isEmpty()) {
                    appendLine("  No payments recorded")
                } else {
                    payments.forEach { payment ->
                        appendLine("  - ${payment.method} : ${payment.amount} (${payment.status})")
                    }
                }
                appendLine("==================")
            }

            // 4. Log audit entry
            val auditEntry = AuditEntry(
                actor = "SYSTEM",
                action = "RECEIPT_GENERATED",
                details = "Receipt generated for order $orderId",
                timestamp = System.currentTimeMillis()
            )
            auditRepository.insertAuditEntry(auditEntry)

            Result.Success(receipt)
        } catch (e: Exception) {
            Result.Failure(e.message ?: "An unexpected error occurred while generating the receipt")
        }
    }
}

// Result type (defined elsewhere in the project)
// sealed class Result<out T> {
//     data class Success<out T>(val data: T) : Result<T>()
//     data class Failure(val message: String) : Result<Nothing>()
// }
