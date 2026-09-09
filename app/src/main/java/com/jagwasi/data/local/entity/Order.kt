package com.jagwasi.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "customer_name")
    val customerName: String,

    @ColumnInfo(name = "items")
    val items: String, // JSON or comma-separated string

    @ColumnInfo(name = "total_amount")
    val totalAmount: Double,

    @ColumnInfo(name = "status")
    val status: String = "PENDING", // "PENDING", "PAID", "COMPLETED"

    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)
