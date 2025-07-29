package com.dietapp.data.seeddata

import com.dietapp.data.entities.Food
import com.dietapp.data.entities.Goal
import com.dietapp.data.entities.WeightEntry
import com.dietapp.data.entities.WaterIntake
import com.dietapp.data.repository.FoodRepository
import com.dietapp.data.repository.GoalRepository
import com.dietapp.data.repository.WeightRepository
import com.dietapp.data.repository.WaterIntakeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeedDataManager @Inject constructor(
    private val foodRepository: FoodRepository,
    private val goalRepository: GoalRepository,
    private val weightRepository: WeightRepository,
    private val waterIntakeRepository: WaterIntakeRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun seedDatabase() {
        scope.launch {
            try {
                val sampleFoods = getSampleFoods()
                foodRepository.insertFoods(sampleFoods)
            } catch (e: Exception) {
                // Silently fail - data might already exist
            }
        }
    }

    fun seedSampleUserData(userId: String) {
        scope.launch {
            try {
                // Seed sample goal
                seedSampleGoal(userId)

                // Seed sample weight entries
                // TEMPORARILY DISABLED: seedSampleWeightEntries(userId)

                // Seed sample water intake for today
                seedSampleWaterIntake(userId)

            } catch (e: Exception) {
                // Silently fail - data might already exist
            }
        }
    }

    private fun getSampleFoods(): List<Food> = listOf(
        Food(
            id = "apple",
            name = "Apple",
            calories = 52.0,
            protein = 0.3,
            carbs = 14.0,
            fat = 0.2,
            fiber = 2.4,
            sugar = 10.0,
            servingSize = 182.0,
            servingUnit = "medium apple"
        ),
        Food(
            id = "banana",
            name = "Banana",
            calories = 89.0,
            protein = 1.1,
            carbs = 23.0,
            fat = 0.3,
            fiber = 2.6,
            sugar = 12.0,
            servingSize = 118.0,
            servingUnit = "medium banana"
        ),
        Food(
            id = "chicken_breast",
            name = "Chicken Breast",
            calories = 165.0,
            protein = 31.0,
            carbs = 0.0,
            fat = 3.6,
            servingSize = 100.0,
            servingUnit = "g"
        ),
        Food(
            id = "brown_rice",
            name = "Brown Rice (cooked)",
            calories = 111.0,
            protein = 2.6,
            carbs = 23.0,
            fat = 0.9,
            fiber = 1.8,
            servingSize = 100.0,
            servingUnit = "g"
        ),
        Food(
            id = "broccoli",
            name = "Broccoli",
            calories = 34.0,
            protein = 2.8,
            carbs = 7.0,
            fat = 0.4,
            fiber = 2.6,
            servingSize = 100.0,
            servingUnit = "g"
        ),
        Food(
            id = "salmon",
            name = "Salmon",
            calories = 208.0,
            protein = 25.0,
            carbs = 0.0,
            fat = 12.0,
            servingSize = 100.0,
            servingUnit = "g"
        ),
        Food(
            id = "oats",
            name = "Oats (dry)",
            calories = 389.0,
            protein = 16.9,
            carbs = 66.0,
            fat = 6.9,
            fiber = 10.6,
            servingSize = 100.0,
            servingUnit = "g"
        ),
        Food(
            id = "eggs",
            name = "Eggs",
            calories = 155.0,
            protein = 13.0,
            carbs = 1.1,
            fat = 11.0,
            servingSize = 100.0,
            servingUnit = "g"
        ),
        Food(
            id = "avocado",
            name = "Avocado",
            calories = 160.0,
            protein = 2.0,
            carbs = 9.0,
            fat = 15.0,
            fiber = 7.0,
            servingSize = 100.0,
            servingUnit = "g"
        ),
        Food(
            id = "greek_yogurt",
            name = "Greek Yogurt (plain)",
            calories = 59.0,
            protein = 10.0,
            carbs = 3.6,
            fat = 0.4,
            servingSize = 100.0,
            servingUnit = "g"
        ),
        Food(
            id = "sweet_potato",
            name = "Sweet Potato",
            calories = 86.0,
            protein = 1.6,
            carbs = 20.0,
            fat = 0.1,
            fiber = 3.0,
            sugar = 4.2,
            servingSize = 100.0,
            servingUnit = "g"
        ),
        Food(
            id = "almonds",
            name = "Almonds",
            calories = 579.0,
            protein = 21.0,
            carbs = 22.0,
            fat = 50.0,
            fiber = 12.0,
            servingSize = 100.0,
            servingUnit = "g"
        ),
        Food(
            id = "spinach",
            name = "Spinach",
            calories = 23.0,
            protein = 2.9,
            carbs = 3.6,
            fat = 0.4,
            fiber = 2.2,
            servingSize = 100.0,
            servingUnit = "g"
        ),
        Food(
            id = "quinoa",
            name = "Quinoa (cooked)",
            calories = 120.0,
            protein = 4.4,
            carbs = 22.0,
            fat = 1.9,
            fiber = 2.8,
            servingSize = 100.0,
            servingUnit = "g"
        ),
        Food(
            id = "milk",
            name = "Milk (2%)",
            calories = 50.0,
            protein = 3.3,
            carbs = 4.8,
            fat = 2.0,
            servingSize = 100.0,
            servingUnit = "ml"
        )
    )

    private suspend fun seedSampleGoal(userId: String) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, 3) // 3 months target

        val sampleGoal = Goal(
            userId = userId,
            goalType = "weight_loss",
            targetWeight = 70.0,
            targetCalories = 1800.0,
            targetProtein = 135.0,
            targetCarbs = 180.0,
            targetFat = 60.0,
            targetDate = calendar.time,
            weeklyWeightLossGoal = 0.5,
            activityLevel = "moderate",
            currentHeight = 175.0, // 5'9" in cm
            useImperialUnits = false, // Can be changed by user preference
            isActive = true
        )

        goalRepository.createNewGoal(sampleGoal)
    }

    private suspend fun seedSampleWeightEntries(userId: String) {
        val calendar = Calendar.getInstance()
        val today = calendar.time

        val weightEntries = mutableListOf<WeightEntry>()

        // Create weight entries for the last 30 days with slight variations
        var currentWeight = 75.0
        for (i in 29 downTo 0) {
            calendar.time = today
            calendar.add(Calendar.DAY_OF_YEAR, -i)

            // Simulate gradual weight loss with some daily fluctuations
            val dailyVariation = (Math.random() - 0.5) * 0.4 // ±0.2kg daily variation
            val trendReduction = i * 0.02 // 0.02kg reduction per day on average
            val weightForDay = currentWeight - trendReduction + dailyVariation

            if (i % 3 == 0) { // Only add entry every 3 days for realistic tracking
                weightEntries.add(
                    WeightEntry(
                        userId = userId,
                        weight = weightForDay,
                        bodyFatPercentage = 18.0 + (Math.random() - 0.5) * 2, // 17-19%
                        date = calendar.time
                    )
                )
            }
        }

        weightEntries.forEach { weightRepository.insertWeightEntry(it) }
    }

    private suspend fun seedSampleWaterIntake(userId: String) {
        val calendar = Calendar.getInstance()
        val now = calendar.time

        // Add some water entries for today at different times
        val waterEntries = listOf(
            // Morning
            calendar.apply {
                time = now
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
            }.time to 500.0, // Large glass

            // Mid-morning
            calendar.apply {
                time = now
                set(Calendar.HOUR_OF_DAY, 10)
                set(Calendar.MINUTE, 30)
            }.time to 250.0, // Cup

            // Lunch
            calendar.apply {
                time = now
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 30)
            }.time to 350.0, // Large glass

            // Afternoon
            calendar.apply {
                time = now
                set(Calendar.HOUR_OF_DAY, 15)
                set(Calendar.MINUTE, 0)
            }.time to 200.0, // Small glass
        )

        waterEntries.forEach { (time, amount) ->
            if (time.before(now)) { // Only add past entries
                waterIntakeRepository.addWaterIntake(userId, amount)
            }
        }
    }
}
