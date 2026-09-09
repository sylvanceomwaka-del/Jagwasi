package com.jagwasi.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jagwasi.data.local.entity.StockItem
import kotlinx.coroutines.flow.Flow

@Dao
interface StockItemDao {
    @Insert
    suspend fun insert(stockItem: StockItem): Long

    @Update
    suspend fun update(stockItem: StockItem)

    @Delete
    suspend fun delete(stockItem: StockItem)

    @Query("SELECT * FROM stock_items")
    fun getAllStockItems(): Flow<List<StockItem>>

    @Query("SELECT * FROM stock_items WHERE id = :id")
    suspend fun getStockItemById(id: Int): StockItem?

    @Query("SELECT * FROM stock_items WHERE quantity <= threshold")
    fun getLowStockItems(): Flow<List<StockItem>>
}
