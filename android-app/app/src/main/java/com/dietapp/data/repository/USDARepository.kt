package com.dietapp.data.repository

import com.dietapp.data.api.USDAApiService
import com.dietapp.data.api.USDAFoodDetail
import com.dietapp.data.api.USDAFoodItem
import com.dietapp.data.api.USDANutrient
import com.dietapp.data.entities.Food
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class USDARepository @Inject constructor(
    private val usdaApiService: USDAApiService
) {
    
    suspend fun searchFoodByBarcode(barcode: String, apiKey: String): Food? {
        return try {
            // Search for food by barcode/UPC
            val searchResponse = usdaApiService.searchFoods(
                query = barcode,
                apiKey = apiKey,
                dataType = "Branded",
                pageSize = 1
            )

            val foodItem = searchResponse.foods?.firstOrNull { food ->
                food.gtinUpc == barcode
            }

            if (foodItem != null) {
                // Get detailed information
                val foodDetail = usdaApiService.getFoodById(foodItem.fdcId, apiKey)
                convertUSDAToFood(foodDetail, barcode)
            } else {
                null
            }
        } catch (e: Exception) {
            println("DEBUG USDARepository: Error searching food by barcode: ${e.message}")
            null
        }
    }

    suspend fun searchFoodsByName(query: String, apiKey: String): List<Food> {
        return try {
            val searchResponse = usdaApiService.searchFoods(
                query = query,
                apiKey = apiKey,
                dataType = "Branded",
                pageSize = 25
            )

            searchResponse.foods?.mapNotNull { foodItem ->
                try {
                    convertUSDAItemToFood(foodItem)
                } catch (e: Exception) {
                    println("DEBUG USDARepository: Error converting food item: ${e.message}")
                    null
                }
            } ?: emptyList()
        } catch (e: Exception) {
            println("DEBUG USDARepository: Error searching foods by name: ${e.message}")
            emptyList()
        }
    }

    private fun convertUSDAToFood(usdaFood: USDAFoodDetail, barcode: String): Food {
        val nutrients = usdaFood.foodNutrients

        return Food(
            id = usdaFood.fdcId.toString(),
            name = usdaFood.description,
            brand = usdaFood.brandOwner ?: usdaFood.brandName ?: "Unknown Brand",
            barcode = barcode,
            servingSize = usdaFood.servingSize ?: 100.0,
            servingUnit = usdaFood.servingSizeUnit ?: "g",
            calories = getNutrientValue(nutrients, USDANutrient.ENERGY_KCAL) ?: 0.0,
            protein = getNutrientValue(nutrients, USDANutrient.PROTEIN) ?: 0.0,
            carbs = getNutrientValue(nutrients, USDANutrient.CARBOHYDRATE) ?: 0.0,
            fat = getNutrientValue(nutrients, USDANutrient.TOTAL_LIPID_FAT) ?: 0.0,
            fiber = getNutrientValue(nutrients, USDANutrient.FIBER) ?: 0.0,
            sugar = getNutrientValue(nutrients, USDANutrient.SUGARS) ?: 0.0,
            sodium = getNutrientValue(nutrients, USDANutrient.SODIUM) ?: 0.0,
            isCustom = false,
            createdAt = Date()
        )
    }

    private fun convertUSDAItemToFood(usdaFood: USDAFoodItem): Food {
        val nutrients = usdaFood.foodNutrients ?: emptyList()

        return Food(
            id = usdaFood.fdcId.toString(),
            name = usdaFood.description,
            brand = usdaFood.brandOwner ?: usdaFood.brandName ?: "Unknown Brand",
            barcode = usdaFood.gtinUpc,
            servingSize = usdaFood.servingSize ?: 100.0,
            servingUnit = usdaFood.servingSizeUnit ?: "g",
            calories = getNutrientValue(nutrients, USDANutrient.ENERGY_KCAL) ?: 0.0,
            protein = getNutrientValue(nutrients, USDANutrient.PROTEIN) ?: 0.0,
            carbs = getNutrientValue(nutrients, USDANutrient.CARBOHYDRATE) ?: 0.0,
            fat = getNutrientValue(nutrients, USDANutrient.TOTAL_LIPID_FAT) ?: 0.0,
            fiber = getNutrientValue(nutrients, USDANutrient.FIBER) ?: 0.0,
            sugar = getNutrientValue(nutrients, USDANutrient.SUGARS) ?: 0.0,
            sodium = getNutrientValue(nutrients, USDANutrient.SODIUM) ?: 0.0,
            isCustom = false,
            createdAt = Date()
        )
    }

    private fun getNutrientValue(nutrients: List<USDANutrient>, nutrientId: Int): Double? {
        return nutrients.find { it.nutrientId == nutrientId }?.value
    }
}
