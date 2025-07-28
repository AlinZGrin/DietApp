package com.dietapp.data.repository

import com.dietapp.data.dao.FoodDao
import com.dietapp.data.entities.Food
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodDataInitializer @Inject constructor(
    private val foodDao: FoodDao
) {

    suspend fun initializeSampleFoodData() {
        // Check if we already have foods in the database
        try {
            val sampleFoods = getSampleFoods()
            foodDao.insertFoods(sampleFoods)
        } catch (e: Exception) {
            // Ignore errors during sample data initialization
        }
    }

    private fun getSampleFoods(): List<Food> {
        return listOf(
            // Common Foods
            Food(
                id = "apple_001",
                name = "Apple",
                brand = "Fresh",
                calories = 52.0,
                protein = 0.3,
                carbs = 14.0,
                fat = 0.2,
                fiber = 2.4,
                sugar = 10.4,
                servingSize = 100.0,
                servingUnit = "g",
                isCustom = false
            ),
            Food(
                id = "banana_001",
                name = "Banana",
                brand = "Fresh",
                calories = 89.0,
                protein = 1.1,
                carbs = 23.0,
                fat = 0.3,
                fiber = 2.6,
                sugar = 12.2,
                servingSize = 100.0,
                servingUnit = "g",
                isCustom = false
            ),
            Food(
                id = "chicken_breast_001",
                name = "Chicken Breast",
                brand = "Raw",
                calories = 165.0,
                protein = 31.0,
                carbs = 0.0,
                fat = 3.6,
                servingSize = 100.0,
                servingUnit = "g",
                isCustom = false
            ),
            Food(
                id = "brown_rice_001",
                name = "Brown Rice",
                brand = "Cooked",
                calories = 123.0,
                protein = 2.6,
                carbs = 23.0,
                fat = 0.9,
                fiber = 1.8,
                servingSize = 100.0,
                servingUnit = "g",
                isCustom = false
            ),
            Food(
                id = "broccoli_001",
                name = "Broccoli",
                brand = "Fresh",
                calories = 34.0,
                protein = 2.8,
                carbs = 7.0,
                fat = 0.4,
                fiber = 2.6,
                servingSize = 100.0,
                servingUnit = "g",
                isCustom = false
            ),
            Food(
                id = "salmon_001",
                name = "Salmon",
                brand = "Atlantic",
                calories = 208.0,
                protein = 25.4,
                carbs = 0.0,
                fat = 12.4,
                servingSize = 100.0,
                servingUnit = "g",
                isCustom = false
            ),
            Food(
                id = "oats_001",
                name = "Oats",
                brand = "Rolled",
                calories = 389.0,
                protein = 16.9,
                carbs = 66.3,
                fat = 6.9,
                fiber = 10.6,
                servingSize = 100.0,
                servingUnit = "g",
                isCustom = false
            ),
            Food(
                id = "greek_yogurt_001",
                name = "Greek Yogurt",
                brand = "Plain",
                calories = 59.0,
                protein = 10.0,
                carbs = 3.6,
                fat = 0.4,
                sugar = 3.6,
                servingSize = 100.0,
                servingUnit = "g",
                isCustom = false
            ),
            Food(
                id = "almonds_001",
                name = "Almonds",
                brand = "Raw",
                calories = 579.0,
                protein = 21.2,
                carbs = 21.6,
                fat = 49.9,
                fiber = 12.5,
                servingSize = 100.0,
                servingUnit = "g",
                isCustom = false
            ),
            Food(
                id = "spinach_001",
                name = "Spinach",
                brand = "Fresh",
                calories = 23.0,
                protein = 2.9,
                carbs = 3.6,
                fat = 0.4,
                fiber = 2.2,
                servingSize = 100.0,
                servingUnit = "g",
                isCustom = false
            ),
            Food(
                id = "sweet_potato_001",
                name = "Sweet Potato",
                brand = "Baked",
                calories = 90.0,
                protein = 2.0,
                carbs = 20.7,
                fat = 0.1,
                fiber = 3.3,
                sugar = 6.8,
                servingSize = 100.0,
                servingUnit = "g",
                isCustom = false
            ),
            Food(
                id = "quinoa_001",
                name = "Quinoa",
                brand = "Cooked",
                calories = 120.0,
                protein = 4.4,
                carbs = 22.0,
                fat = 1.9,
                fiber = 2.8,
                servingSize = 100.0,
                servingUnit = "g",
                isCustom = false
            ),
            Food(
                id = "avocado_001",
                name = "Avocado",
                brand = "Fresh",
                calories = 160.0,
                protein = 2.0,
                carbs = 8.5,
                fat = 14.7,
                fiber = 6.7,
                servingSize = 100.0,
                servingUnit = "g",
                isCustom = false
            ),
            Food(
                id = "eggs_001",
                name = "Eggs",
                brand = "Large",
                calories = 155.0,
                protein = 13.0,
                carbs = 1.1,
                fat = 11.0,
                servingSize = 100.0,
                servingUnit = "g",
                isCustom = false
            ),
            Food(
                id = "whole_milk_001",
                name = "Whole Milk",
                brand = "Regular",
                calories = 61.0,
                protein = 3.2,
                carbs = 4.8,
                fat = 3.3,
                sugar = 5.1,
                servingSize = 100.0,
                servingUnit = "ml",
                isCustom = false
            )
        )
    }
}
