package com.jagwasi.data.repository

import com.jagwasi.data.local.entity.Payment
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    suspend fun insertPayment(payment: Payment): Long
    suspend fun updatePayment(payment: Payment)
    suspend fun deletePayment(payment: Payment)
    fun getPaymentsByOrderId(orderId: Int): Flow<List<Payment>>
    fun getPaymentsByStatus(status: String): Flow<List<Payment>>
    fun getAllPayments(): Flow<List<Payment>>
}
