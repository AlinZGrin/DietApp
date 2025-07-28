package com.dietapp.data.repository

import com.dietapp.data.dao.WaterIntakeDao
import com.dietapp.data.entities.WaterIntake
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WaterIntakeRepository @Inject constructor(
    private val waterIntakeDao: WaterIntakeDao
) {
    fun getTodaysWaterIntake(userId: String): Flow<List<WaterIntake>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = calendar.time

        return waterIntakeDao.getTodaysWaterIntake(userId, startOfDay, endOfDay)
    }

    suspend fun getTotalWaterIntakeForToday(userId: String): Double {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = calendar.time

        return waterIntakeDao.getTotalWaterIntakeForDay(userId, startOfDay, endOfDay) ?: 0.0
    }

    fun getWaterIntakeInRange(userId: String, startDate: Date, endDate: Date): Flow<List<WaterIntake>> =
        waterIntakeDao.getWaterIntakeInRange(userId, startDate, endDate)

    suspend fun addWaterIntake(userId: String, amount: Double): Long {
        val waterIntake = WaterIntake(
            userId = userId,
            amount = amount,
            date = Date()
        )
        return waterIntakeDao.insertWaterIntake(waterIntake)
    }

    suspend fun updateWaterIntake(waterIntake: WaterIntake) =
        waterIntakeDao.updateWaterIntake(waterIntake)

    suspend fun deleteWaterIntake(waterIntake: WaterIntake) =
        waterIntakeDao.deleteWaterIntake(waterIntake)

    suspend fun clearTodaysWaterIntake(userId: String) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = calendar.time

        waterIntakeDao.clearTodaysWaterIntake(userId, startOfDay, endOfDay)
    }

    /**
     * Calculate recommended daily water intake based on weight and activity level
     */
    fun calculateRecommendedWaterIntake(weightKg: Double, activityLevel: String): Double {
        // Base: 35ml per kg of body weight
        val baseIntake = weightKg * 35

        // Activity multiplier
        val activityMultiplier = when (activityLevel) {
            "sedentary" -> 1.0
            "light" -> 1.1
            "moderate" -> 1.2
            "active" -> 1.3
            "very_active" -> 1.4
            else -> 1.0
        }

        return baseIntake * activityMultiplier
    }

    /**
     * Get hydration status based on intake vs recommendation
     */
    fun getHydrationStatus(currentIntake: Double, recommendedIntake: Double): String {
        val percentage = (currentIntake / recommendedIntake) * 100

        return when {
            percentage >= 100 -> "Excellent"
            percentage >= 80 -> "Good"
            percentage >= 60 -> "Moderate"
            percentage >= 40 -> "Low"
            else -> "Very Low"
        }
    }

    /**
     * Common water serving sizes in ml
     */
    fun getCommonServingSizes(): List<Pair<String, Double>> {
        return listOf(
            "Small glass" to 200.0,
            "Large glass" to 350.0,
            "Water bottle" to 500.0,
            "Large bottle" to 750.0,
            "Cup" to 250.0,
            "Mug" to 300.0
        )
    }
}
