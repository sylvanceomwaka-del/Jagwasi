package com.jagwasi.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_cards")
data class MealCard(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "order_id")
    val orderId: Int,

    @ColumnInfo(name = "customer_name")
    val customerName: String,

    @ColumnInfo(name = "items")
    val items: String, // JSON format

    @ColumnInfo(name = "total_amount")
    val totalAmount: Double,

    @ColumnInfo(name = "status")
    val status: String = "PREPARING", // "PREPARING", "READY", "SERVED", "CANCELLED"

    @ColumnInfo(name = "timestamp")
    val timestamp: Long // caller must provide System.currentTimeMillis()
)
