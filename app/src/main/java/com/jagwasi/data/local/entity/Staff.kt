package com.jagwasi.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "staff")
data class Staff(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "role")
    val role: String, // "MASTER", "SUPERVISOR", "RUNNER", "KITCHEN"

    @ColumnInfo(name = "pin_hash")
    val pinHash: String, // hashed PIN for authentication

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true, // default true

    @ColumnInfo(name = "timestamp")
    val timestamp: Long // when the account was created, caller must provide
)
