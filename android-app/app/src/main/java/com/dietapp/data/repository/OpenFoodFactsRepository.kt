package com.dietapp.data.repository

import com.dietapp.data.api.OpenFoodFactsApiService
import com.dietapp.data.api.OpenFoodFactsProduct
import com.dietapp.data.entities.Food
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenFoodFactsRepository @Inject constructor(
    private val openFoodFactsApiService: OpenFoodFactsApiService
) {

    suspend fun searchFoodByBarcode(barcode: String): Food? {
        return try {
            val response = openFoodFactsApiService.getProductByBarcode(barcode)

            if (response.status == 1 && response.product != null) {
                convertOpenFoodFactsToFood(response.product, barcode)
            } else {
                null
            }
        } catch (e: Exception) {
            println("DEBUG OpenFoodFactsRepository: Error searching food by barcode: ${e.message}")
            null
        }
    }

    private fun convertOpenFoodFactsToFood(product: OpenFoodFactsProduct, barcode: String): Food {
        val nutrients = product.nutriments

        return Food(
            id = barcode,
            name = product.product_name ?: "Unknown Product",
            brand = product.brands ?: "Unknown Brand",
            barcode = barcode,
            servingSize = parseServingSize(product.serving_size ?: product.quantity),
            servingUnit = "g",
            calories = nutrients?.energy_kcal_100g ?: 0.0,
            protein = nutrients?.proteins_100g ?: 0.0,
            carbs = nutrients?.carbohydrates_100g ?: 0.0,
            fat = nutrients?.fat_100g ?: 0.0,
            fiber = nutrients?.fiber_100g ?: 0.0,
            sugar = nutrients?.sugars_100g ?: 0.0,
            sodium = (nutrients?.sodium_100g ?: (nutrients?.salt_100g?.div(2.5))) ?: 0.0, // Convert salt to sodium
            isCustom = false,
            createdAt = Date()
        )
    }

    private fun parseServingSize(servingText: String?): Double {
        if (servingText == null) return 100.0

        val regex = Regex("(\\d+(?:\\.\\d+)?)")
        val match = regex.find(servingText)
        return match?.value?.toDoubleOrNull() ?: 100.0
    }
}
