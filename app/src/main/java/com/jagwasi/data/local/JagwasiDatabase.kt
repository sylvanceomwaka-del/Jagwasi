package com.jagwasi.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jagwasi.data.local.entity.Order
import com.jagwasi.data.local.entity.MealCard
import com.jagwasi.data.local.entity.StockItem
import com.jagwasi.data.local.entity.Payment
import com.jagwasi.data.local.entity.Staff
import com.jagwasi.data.local.entity.AuditEntry
import com.jagwasi.data.local.dao.OrderDao
import com.jagwasi.data.local.dao.MealCardDao
import com.jagwasi.data.local.dao.StockItemDao
import com.jagwasi.data.local.dao.PaymentDao
import com.jagwasi.data.local.dao.StaffDao
import com.jagwasi.data.local.dao.AuditEntryDao

@Database(
    entities = [
        Order::class,
        MealCard::class,
        StockItem::class,
        Payment::class,
        Staff::class,
        AuditEntry::class
    ],
    version = 1,
    exportSchema = true
)
abstract class JagwasiDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
    abstract fun mealCardDao(): MealCardDao
    abstract fun stockItemDao(): StockItemDao
    abstract fun paymentDao(): PaymentDao
    abstract fun staffDao(): StaffDao
    abstract fun auditEntryDao(): AuditEntryDao

    companion object {
        const val DATABASE_NAME = "jagwasi_secure_db"
    }
}
