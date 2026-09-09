// MealCardRepository.kt (corrected interface)
package com.jagwasi.data.repository

import com.jagwasi.data.local.entity.MealCard
import kotlinx.coroutines.flow.Flow

interface MealCardRepository {
    suspend fun insertMealCard(mealCard: MealCard): Long
    suspend fun updateMealCard(mealCard: MealCard)
    suspend fun deleteMealCard(mealCard: MealCard)
    fun getMealCardsByStatus(status: String): Flow<List<MealCard>>
    suspend fun getMealCardByOrderId(orderId: Int): MealCard?
    fun getAllMealCards(): Flow<List<MealCard>>
}
// MealCardRepositoryImpl.kt (corrected implementation)
package com.jagwasi.data.repository

import com.jagwasi.data.local.dao.MealCardDao
import com.jagwasi.data.local.entity.MealCard
import kotlinx.coroutines.flow.Flow

class MealCardRepositoryImpl(
    private val mealCardDao: MealCardDao
) : MealCardRepository {

    override suspend fun insertMealCard(mealCard: MealCard): Long {
        return mealCardDao.insert(mealCard)
    }

    override suspend fun updateMealCard(mealCard: MealCard) {
        mealCardDao.update(mealCard)
    }

    override suspend fun deleteMealCard(mealCard: MealCard) {
        mealCardDao.delete(mealCard)
    }

    override fun getMealCardsByStatus(status: String): Flow<List<MealCard>> {
        return mealCardDao.getMealCardsByStatus(status)
    }

    override suspend fun getMealCardByOrderId(orderId: Int): MealCard? {
        return mealCardDao.getMealCardByOrderId(orderId)
    }

    override fun getAllMealCards(): Flow<List<MealCard>> {
        return mealCardDao.getAllMealCards()
    }
}
