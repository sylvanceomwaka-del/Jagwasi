package com.jagwasi.data.repository

import com.jagwasi.data.local.dao.StockItemDao
import com.jagwasi.data.local.entity.StockItem
import kotlinx.coroutines.flow.Flow

class StockItemRepositoryImpl(
    private val stockItemDao: StockItemDao
) : StockItemRepository {

    override suspend fun insertStockItem(stockItem: StockItem): Long {
        return stockItemDao.insert(stockItem)
    }

    override suspend fun updateStockItem(stockItem: StockItem) {
        stockItemDao.update(stockItem)
    }

    override suspend fun deleteStockItem(stockItem: StockItem) {
        stockItemDao.delete(stockItem)
    }

    override fun getAllStockItems(): Flow<List<StockItem>> {
        return stockItemDao.getAllStockItems()
    }

    override suspend fun getStockItemById(id: Int): StockItem? {
        return stockItemDao.getStockItemById(id)
    }

    override fun getLowStockItems(): Flow<List<StockItem>> {
        return stockItemDao.getLowStockItems()
    }
}
