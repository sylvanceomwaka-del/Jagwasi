package com.jagwasi.data.repository

import com.jagwasi.data.local.entity.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun insertOrder(order: Order): Long
    suspend fun updateOrder(order: Order)
    suspend fun deleteOrder(order: Order)
    fun getOrdersByStatus(status: String): Flow<List<Order>>
    suspend fun getOrderById(id: Int): Order?
}
