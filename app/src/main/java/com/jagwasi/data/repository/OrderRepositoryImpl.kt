package com.jagwasi.data.repository

import com.jagwasi.data.local.dao.OrderDao
import com.jagwasi.data.local.entity.Order
import kotlinx.coroutines.flow.Flow

class OrderRepositoryImpl(
    private val orderDao: OrderDao
) : OrderRepository {

    override suspend fun insertOrder(order: Order): Long {
        return orderDao.insert(order)
    }

    override suspend fun updateOrder(order: Order) {
        orderDao.update(order)
    }

    override suspend fun deleteOrder(order: Order) {
        orderDao.delete(order)
    }

    override fun getOrdersByStatus(status: String): Flow<List<Order>> {
        return orderDao.getOrdersByStatus(status)
    }

    override suspend fun getOrderById(id: Int): Order? {
        return orderDao.getOrderById(id)
    }
}
