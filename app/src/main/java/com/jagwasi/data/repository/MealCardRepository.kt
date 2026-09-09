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
