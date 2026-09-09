package com.jagwasi.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "order_id")
    val orderId: Int,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "method")
    val method: String, // "CASH", "M-PESA", "CARD"

    @ColumnInfo(name = "status")
    val status: String = "PENDING", // "PENDING", "COMPLETED", "FAILED"

    @ColumnInfo(name = "transaction_id")
    val transactionId: String? = null, // for M-PESA or card

    @ColumnInfo(name = "timestamp")
    val timestamp: Long // caller must provide System.currentTimeMillis()
)
