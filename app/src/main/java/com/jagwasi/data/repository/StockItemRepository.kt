package com.jagwasi.data.repository

import com.jagwasi.data.local.entity.StockItem
import kotlinx.coroutines.flow.Flow

interface StockItemRepository {
    suspend fun insertStockItem(stockItem: StockItem): Long
    suspend fun updateStockItem(stockItem: StockItem)
    suspend fun deleteStockItem(stockItem: StockItem)
    fun getAllStockItems(): Flow<List<StockItem>>
    suspend fun getStockItemById(id: Int): StockItem?
    fun getLowStockItems(): Flow<List<StockItem>>
}
