package com.jagwasi.data.repository

import com.jagwasi.data.local.dao.PaymentDao
import com.jagwasi.data.local.entity.Payment
import kotlinx.coroutines.flow.Flow

class PaymentRepositoryImpl(
    private val paymentDao: PaymentDao
) : PaymentRepository {

    override suspend fun insertPayment(payment: Payment): Long {
        return paymentDao.insert(payment)
    }

    override suspend fun updatePayment(payment: Payment) {
        paymentDao.update(payment)
    }

    override suspend fun deletePayment(payment: Payment) {
        paymentDao.delete(payment)
    }

    override fun getPaymentsByOrderId(orderId: Int): Flow<List<Payment>> {
        return paymentDao.getPaymentsByOrderId(orderId)
    }

    override fun getPaymentsByStatus(status: String): Flow<List<Payment>> {
        return paymentDao.getPaymentsByStatus(status)
    }

    override fun getAllPayments(): Flow<List<Payment>> {
        return paymentDao.getAllPayments()
    }
}
