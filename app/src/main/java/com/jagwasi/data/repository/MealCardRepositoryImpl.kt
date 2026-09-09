package com.jagwasi.data.repository
import com.jagwasi.data.local.entity.MealCard
import kotlinx.coroutines.flow.Flow
import com.jagwasi.data.local.dao.MealCardDao
import com.jagwasi.data.local.entity.MealCard
import kotlinx.coroutines.flow.Flow
// MealCardRepository.kt (corrected interface)


// MealCardRepositoryImpl.kt (corrected implementation)


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
