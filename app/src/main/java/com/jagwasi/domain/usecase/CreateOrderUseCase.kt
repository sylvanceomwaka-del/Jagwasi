package com.jagwasi.domain.usecase

import com.jagwasi.data.local.entity.AuditEntry
import com.jagwasi.data.local.entity.MealCard
import com.jagwasi.data.local.entity.Order
import com.jagwasi.data.repository.AuditEntryRepository
import com.jagwasi.data.repository.MealCardRepository
import com.jagwasi.data.repository.OrderRepository
import com.jagwasi.data.repository.StockItemRepository

/**
 * Abstraction for a transaction scope that allows running a block of operations
 * atomically. The concrete implementation (e.g., using Room withTransaction)
 * is injected and kept outside the domain layer.
 */
interface TransactionProvider {
    suspend fun <T> withTransaction(block: suspend () -> T): T
}

/**
 * Use case for creating a new order with atomic stock deduction,
 * order persistence, meal card creation, and audit logging.
 *
 * @param orderRepository Repository for Order entities
 * @param mealCardRepository Repository for MealCard entities
 * @param stockRepository Repository for StockItem entities
 * @param auditRepository Repository for AuditEntry entities
 * @param transactionProvider Provider for atomic transactions
 */
class CreateOrderUseCase(
    private val orderRepository: OrderRepository,
    private val mealCardRepository: MealCardRepository,
    private val stockRepository: StockItemRepository,
    private val auditRepository: AuditEntryRepository,
    private val transactionProvider: TransactionProvider
) {
    suspend operator fun invoke(order: Order, items: List<Pair<Int, Double>>): Result<Order> {
        return try {
            transactionProvider.withTransaction {
                // 1. Validate and deduct stock atomically
                for ((itemId, quantity) in items) {
                    val stockItem = stockRepository.getStockItemById(itemId)
                        ?: throw IllegalStateException("Stock item with ID $itemId not found")
                    if (stockItem.quantity < quantity) {
                        throw IllegalStateException(
                            "Insufficient stock for item: ${stockItem.name} " +
                            "(available: ${stockItem.quantity}, requested: $quantity)"
                        )
                    }
                    val updatedItem = stockItem.copy(quantity = stockItem.quantity - quantity)
                    stockRepository.updateStockItem(updatedItem)
                }

                // 2. Insert order
                val orderId = orderRepository.insertOrder(order)
                val insertedOrder = order.copy(id = orderId.toInt())

                // 3. Create and insert meal card
                val mealCard = MealCard(
                    orderId = insertedOrder.id,
                    customerName = insertedOrder.customerName,
                    items = items.joinToString(",") { "${it.first}:${it.second}" },
                    totalAmount = insertedOrder.totalAmount,
                    status = "PREPARING",
                    timestamp = System.currentTimeMillis()
                )
                mealCardRepository.insertMealCard(mealCard)

                // 4. Log audit entry
                val auditEntry = AuditEntry(
                    actor = "SYSTEM",
                    action = "ORDER_CREATED",
                    details = "Order ${insertedOrder.id} created for ${insertedOrder.customerName} with items: ${mealCard.items}",
                    timestamp = System.currentTimeMillis()
                )
                auditRepository.insertAuditEntry(auditEntry)

                Result.Success(insertedOrder)
            }
        } catch (e: Exception) {
            Result.Failure(e.message ?: "An unexpected error occurred while creating the order")
        }
    }
}

// Result type (defined elsewhere in the project)
// sealed class Result<out T> {
//     data class Success<out T>(val data: T) : Result<T>()
//     data class Failure(val message: String) : Result<Nothing>()
// }
