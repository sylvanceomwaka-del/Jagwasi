package com.jagwasi.domain.usecase

import com.jagwasi.data.local.entity.AuditEntry
import com.jagwasi.data.repository.AuditEntryRepository
import com.jagwasi.data.repository.OrderRepository

/**
 * Use case for detecting unpaid exit attempts.
 *
 * @param orderRepository Repository for Order entities
 * @param auditRepository Repository for AuditEntry entities
 */
class DetectUnpaidExitUseCase(
    private val orderRepository: OrderRepository,
    private val auditRepository: AuditEntryRepository
) {
    suspend operator fun invoke(orderId: Int): Result<Boolean> {
        return try {
            // 1. Fetch the order
            val order = orderRepository.getOrderById(orderId)
                ?: return Result.Failure("Order not found")

            // 2. Check if order is already paid or completed
            if (order.status == "PAID" || order.status == "COMPLETED") {
                return Result.Success(false)
            }

            // 3. Log audit entry for unpaid exit
            val auditEntry = AuditEntry(
                actor = "SYSTEM",
                action = "UNPAID_EXIT_DETECTED",
                details = "Unpaid exit detected for order $orderId",
                timestamp = System.currentTimeMillis()
            )
            auditRepository.insertAuditEntry(auditEntry)

            // 4. Return true to indicate unpaid exit detected
            Result.Success(true)
        } catch (e: Exception) {
            Result.Failure(e.message ?: "An unexpected error occurred while detecting unpaid exit")
        }
    }
}

// Result type (defined elsewhere in the project)
// sealed class Result<out T> {
//     data class Success<out T>(val data: T) : Result<T>()
//     data class Failure(val message: String) : Result<Nothing>()
// }
