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
            println("DEBUG OpenFoodFactsRepository: Searching for barcode: $barcode")
            val response = openFoodFactsApiService.getProductByBarcode(barcode)

            println("DEBUG OpenFoodFactsRepository: API response status: ${response.status}")
            println("DEBUG OpenFoodFactsRepository: Product found: ${response.product != null}")

            if (response.status == 1 && response.product != null) {
                val food = convertOpenFoodFactsToFood(response.product, barcode)
                println("DEBUG OpenFoodFactsRepository: Converted food - calories: ${food.calories}, protein: ${food.protein}")
                food
            } else {
                println("DEBUG OpenFoodFactsRepository: No product found for barcode $barcode")
                null
            }
        } catch (e: Exception) {
            println("DEBUG OpenFoodFactsRepository: Error searching food by barcode: ${e.message}")
            e.printStackTrace()
            null
        }
    }    private fun convertOpenFoodFactsToFood(product: OpenFoodFactsProduct, barcode: String): Food {
        val nutrients = product.nutriments

        println("DEBUG OpenFoodFactsRepository: Product name: ${product.product_name}")
        println("DEBUG OpenFoodFactsRepository: Nutriments object: $nutrients")

        // Try multiple field variations for calories
        val calories = nutrients?.energy_kcal_100g
            ?: nutrients?.energy_kcal_per_100g
            ?: (nutrients?.energy_100g?.div(4.184)) // Convert kJ to kcal if needed
            ?: nutrients?.energy
            ?: 0.0

        // Try multiple field variations for protein
        val protein = nutrients?.proteins_100g
            ?: nutrients?.proteins_per_100g
            ?: nutrients?.proteins
            ?: 0.0

        // Try multiple field variations for carbs
        val carbs = nutrients?.carbohydrates_100g
            ?: nutrients?.carbohydrates_per_100g
            ?: nutrients?.carbohydrates
            ?: 0.0

        // Try multiple field variations for fat
        val fat = nutrients?.fat_100g
            ?: nutrients?.fat_per_100g
            ?: nutrients?.fat
            ?: 0.0

        // Try multiple field variations for fiber
        val fiber = nutrients?.fiber_100g
            ?: nutrients?.fiber_per_100g
            ?: nutrients?.fiber
            ?: 0.0

        // Try multiple field variations for sugar
        val sugar = nutrients?.sugars_100g
            ?: nutrients?.sugars_per_100g
            ?: nutrients?.sugars
            ?: 0.0

        // Try multiple field variations for sodium
        val sodium = nutrients?.sodium_100g
            ?: (nutrients?.salt_100g?.div(2.5))
            ?: nutrients?.sodium
            ?: (nutrients?.salt?.div(2.5))
            ?: 0.0

        println("DEBUG OpenFoodFactsRepository: Parsed values - calories: $calories, protein: $protein, carbs: $carbs, fat: $fat")

        return Food(
            id = barcode,
            name = product.product_name ?: "Unknown Product",
            brand = product.brands ?: "Unknown Brand",
            barcode = barcode,
            servingSize = parseServingSize(product.serving_size ?: product.quantity),
            servingUnit = "g",
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            fiber = fiber,
            sugar = sugar,
            sodium = sodium,
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
