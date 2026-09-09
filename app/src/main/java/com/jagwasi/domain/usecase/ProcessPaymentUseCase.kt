package com.jagwasi.domain.usecase

import com.jagwasi.data.local.entity.AuditEntry
import com.jagwasi.data.local.entity.Order
import com.jagwasi.data.local.entity.Payment
import com.jagwasi.data.repository.AuditEntryRepository
import com.jagwasi.data.repository.OrderRepository
import com.jagwasi.data.repository.PaymentRepository

/**
 * Use case for processing a payment for an existing order.
 * All writes are performed atomically inside a transaction.
 *
 * @param paymentRepository Repository for Payment entities
 * @param orderRepository Repository for Order entities
 * @param auditRepository Repository for AuditEntry entities
 * @param transactionProvider Provider for atomic transactions
 */
class ProcessPaymentUseCase(
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository,
    private val auditRepository: AuditEntryRepository,
    private val transactionProvider: TransactionProvider
) {
    suspend operator fun invoke(payment: Payment): Result<Payment> {
        return try {
            transactionProvider.withTransaction {
                // 1. Validate that the order exists
                val order = orderRepository.getOrderById(payment.orderId)
                    ?: throw IllegalStateException("Order with ID ${payment.orderId} not found")

                // 2. Insert the payment
                val paymentId = paymentRepository.insertPayment(payment)
                val insertedPayment = payment.copy(id = paymentId.toInt())

                // 3. Update the order status to "PAID"
                val updatedOrder = order.copy(status = "PAID")
                orderRepository.updateOrder(updatedOrder)

                // 4. Log audit entry
                val auditEntry = AuditEntry(
                    actor = "SYSTEM",
                    action = "PAYMENT_CONFIRMED",
                    details = "Payment ${insertedPayment.id} of ${insertedPayment.amount} confirmed for order ${order.id}",
                    timestamp = System.currentTimeMillis()
                )
                auditRepository.insertAuditEntry(auditEntry)

                Result.Success(insertedPayment)
            }
        } catch (e: Exception) {
            Result.Failure(e.message ?: "An unexpected error occurred while processing the payment")
        }
    }
}

// Result type (defined elsewhere in the project)
// sealed class Result<out T> {
//     data class Success<out T>(val data: T) : Result<T>()
//     data class Failure(val message: String) : Result<Nothing>()
// }
