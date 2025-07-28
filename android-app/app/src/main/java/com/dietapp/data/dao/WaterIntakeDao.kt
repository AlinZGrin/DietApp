package com.dietapp.data.dao

import androidx.room.*
import com.dietapp.data.entities.WaterIntake
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface WaterIntakeDao {
    @Query("SELECT * FROM water_intake WHERE userId = :userId AND date >= :startOfDay AND date < :endOfDay ORDER BY createdAt DESC")
    fun getTodaysWaterIntake(userId: String, startOfDay: Date, endOfDay: Date): Flow<List<WaterIntake>>

    @Query("SELECT SUM(amount) FROM water_intake WHERE userId = :userId AND date >= :startOfDay AND date < :endOfDay")
    suspend fun getTotalWaterIntakeForDay(userId: String, startOfDay: Date, endOfDay: Date): Double?

    @Query("SELECT * FROM water_intake WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getWaterIntakeInRange(userId: String, startDate: Date, endDate: Date): Flow<List<WaterIntake>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterIntake(waterIntake: WaterIntake): Long

    @Update
    suspend fun updateWaterIntake(waterIntake: WaterIntake)

    @Delete
    suspend fun deleteWaterIntake(waterIntake: WaterIntake)

    @Query("DELETE FROM water_intake WHERE userId = :userId AND date >= :startOfDay AND date < :endOfDay")
    suspend fun clearTodaysWaterIntake(userId: String, startOfDay: Date, endOfDay: Date)
}
