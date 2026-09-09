package com.jagwasi.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jagwasi.data.local.entity.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Insert
    suspend fun insert(payment: Payment): Long

    @Update
    suspend fun update(payment: Payment)

    @Delete
    suspend fun delete(payment: Payment)

    @Query("SELECT * FROM payments WHERE order_id = :orderId")
    fun getPaymentsByOrderId(orderId: Int): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE status = :status")
    fun getPaymentsByStatus(status: String): Flow<List<Payment>>

    @Query("SELECT * FROM payments")
    fun getAllPayments(): Flow<List<Payment>>
}
