package com.dietapp.data.dao

import androidx.room.*
import com.dietapp.data.entities.FoodLog
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface FoodLogDao {
    @Query("SELECT * FROM food_logs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    fun getFoodLogsByDateRange(userId: String, startDate: Date, endDate: Date): Flow<List<FoodLog>>

    @Query("SELECT * FROM food_logs WHERE userId = :userId AND date(date/1000, 'unixepoch') = date(:date/1000, 'unixepoch') ORDER BY createdAt DESC")
    fun getFoodLogsByDate(userId: String, date: Date): Flow<List<FoodLog>>

    @Query("SELECT * FROM food_logs WHERE userId = :userId AND mealType = :mealType AND date(date/1000, 'unixepoch') = date(:date/1000, 'unixepoch') ORDER BY createdAt DESC")
    fun getFoodLogsByMealAndDate(userId: String, mealType: String, date: Date): Flow<List<FoodLog>>

    @Query("SELECT SUM(calories) FROM food_logs WHERE userId = :userId AND date(date/1000, 'unixepoch') = date(:date/1000, 'unixepoch')")
    suspend fun getTotalCaloriesForDate(userId: String, date: Date): Double?

    @Query("SELECT SUM(protein) FROM food_logs WHERE userId = :userId AND date(date/1000, 'unixepoch') = date(:date/1000, 'unixepoch')")
    suspend fun getTotalProteinForDate(userId: String, date: Date): Double?

    @Query("SELECT SUM(carbs) FROM food_logs WHERE userId = :userId AND date(date/1000, 'unixepoch') = date(:date/1000, 'unixepoch')")
    suspend fun getTotalCarbsForDate(userId: String, date: Date): Double?

    @Query("SELECT SUM(fat) FROM food_logs WHERE userId = :userId AND date(date/1000, 'unixepoch') = date(:date/1000, 'unixepoch')")
    suspend fun getTotalFatForDate(userId: String, date: Date): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodLog(foodLog: FoodLog)

    @Update
    suspend fun updateFoodLog(foodLog: FoodLog)

    @Delete
    suspend fun deleteFoodLog(foodLog: FoodLog)

    @Query("DELETE FROM food_logs WHERE userId = :userId")
    suspend fun deleteAllFoodLogs(userId: String)
}
