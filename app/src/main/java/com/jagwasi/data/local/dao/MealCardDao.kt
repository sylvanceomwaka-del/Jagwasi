package com.jagwasi.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jagwasi.data.local.entity.MealCard
import kotlinx.coroutines.flow.Flow

@Dao
interface MealCardDao {
    @Insert
    suspend fun insert(mealCard: MealCard): Long

    @Update
    suspend fun update(mealCard: MealCard)

    @Delete
    suspend fun delete(mealCard: MealCard)

    @Query("SELECT * FROM meal_cards WHERE status = :status")
    fun getMealCardsByStatus(status: String): Flow<List<MealCard>>

    @Query("SELECT * FROM meal_cards WHERE order_id = :orderId")
    suspend fun getMealCardByOrderId(orderId: Int): MealCard?

    @Query("SELECT * FROM meal_cards")
    fun getAllMealCards(): Flow<List<MealCard>>
}
