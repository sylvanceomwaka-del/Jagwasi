package com.jagwasi.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_entries")
data class AuditEntry(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "actor")
    val actor: String, // who performed the action, e.g., staff name or "SYSTEM"

    @ColumnInfo(name = "action")
    val action: String, // e.g., "ORDER_CREATED", "PAYMENT_CONFIRMED", "STOCK_DEDUCTED"

    @ColumnInfo(name = "details")
    val details: String, // JSON or human-readable description

    @ColumnInfo(name = "timestamp")
    val timestamp: Long // caller must provide System.currentTimeMillis()
)
