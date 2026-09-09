package com.jagwasi.domain.usecase

import com.jagwasi.data.local.entity.AuditEntry
import com.jagwasi.data.repository.AuditEntryRepository
import com.jagwasi.data.repository.StockItemRepository

/**
 * Use case for deducting stock quantities for multiple items atomically.
 *
 * @param stockRepository Repository for StockItem entities
 * @param auditRepository Repository for AuditEntry entities
 * @param transactionProvider Provider for atomic transactions
 */
class ApplyStockDeductionUseCase(
    private val stockRepository: StockItemRepository,
    private val auditRepository: AuditEntryRepository,
    private val transactionProvider: TransactionProvider
) {
    suspend operator fun invoke(items: List<Pair<Int, Double>>): Result<Unit> {
        return try {
            transactionProvider.withTransaction {
                for ((itemId, quantityToDeduct) in items) {
                    // Fetch stock item
                    val stockItem = stockRepository.getStockItemById(itemId)
                        ?: throw IllegalStateException("Stock item not found: $itemId")

                    // Check sufficient stock
                    if (quantityToDeduct > stockItem.quantity) {
                        throw IllegalStateException("Insufficient stock for: ${stockItem.name} (available: ${stockItem.quantity}, requested: $quantityToDeduct)")
                    }

                    // Update stock
                    val updatedItem = stockItem.copy(quantity = stockItem.quantity - quantityToDeduct)
                    stockRepository.updateStockItem(updatedItem)

                    // Log audit entry for each deduction
                    val auditEntry = AuditEntry(
                        actor = "SYSTEM",
                        action = "STOCK_DEDUCTED",
                        details = "Deducted $quantityToDeduct ${stockItem.unit} from ${stockItem.name} (ID: $itemId)",
                        timestamp = System.currentTimeMillis()
                    )
                    auditRepository.insertAuditEntry(auditEntry)
                }
                Result.Success(Unit)
            }
        } catch (e: Exception) {
            Result.Failure(e.message ?: "An unexpected error occurred while applying stock deductions")
        }
    }
}

// Result type (defined elsewhere in the project)
// sealed class Result<out T> {
//     data class Success<out T>(val data: T) : Result<T>()
//     data class Failure(val message: String) : Result<Nothing>()
// }
