package com.dietapp.data.api

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * OpenFoodFacts API service interface
 * Documentation: https://openfoodfacts.github.io/api-documentation/
 */
interface OpenFoodFactsApiService {

    /**
     * Get product information by barcode
     */
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): OpenFoodFactsResponse
}

/**
 * OpenFoodFacts Response models
 */
data class OpenFoodFactsResponse(
    val status: Int,
    val status_verbose: String,
    val product: OpenFoodFactsProduct?
)

data class OpenFoodFactsProduct(
    val product_name: String?,
    val brands: String?,
    val quantity: String?,
    val serving_size: String?,
    val nutriments: OpenFoodFactsNutriments?,
    val ingredients_text: String?,
    val image_url: String?
)

data class OpenFoodFactsNutriments(
    // Main nutrition fields per 100g
    val energy_100g: Double?,
    val energy_kcal_100g: Double?,
    val proteins_100g: Double?,
    val carbohydrates_100g: Double?,
    val fat_100g: Double?,
    val fiber_100g: Double?,
    val sugars_100g: Double?,
    val salt_100g: Double?,
    val sodium_100g: Double?,

    // Alternative field names with dashes (using @SerializedName)
    val energy_kcal_per_100g: Double?,
    val proteins_per_100g: Double?,
    val carbohydrates_per_100g: Double?,
    val fat_per_100g: Double?,
    val fiber_per_100g: Double?,
    val sugars_per_100g: Double?,

    // Sometimes the values are just named without _100g
    val energy: Double?,
    val proteins: Double?,
    val carbohydrates: Double?,
    val fat: Double?,
    val fiber: Double?,
    val sugars: Double?,
    val salt: Double?,
    val sodium: Double?
)
