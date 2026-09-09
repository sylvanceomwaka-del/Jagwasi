package com.jagwasi.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_items")
data class StockItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "quantity")
    val quantity: Double, // e.g., 10.5 kg

    @ColumnInfo(name = "unit")
    val unit: String, // e.g., "kg", "pcs", "liters"

    @ColumnInfo(name = "threshold")
    val threshold: Double, // minimum stock alert level

    @ColumnInfo(name = "cost_per_unit")
    val costPerUnit: Double,

    @ColumnInfo(name = "last_restock_date")
    val lastRestockDate: Long, // timestamp

    @ColumnInfo(name = "note")
    val note: String? = null // optional
)
