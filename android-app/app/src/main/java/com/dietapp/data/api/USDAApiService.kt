package com.dietapp.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * USDA FoodData Central API service interface
 * Documentation: https://fdc.nal.usda.gov/api-guide.html
 */
interface USDAApiService {

    /**
     * Search for foods by query (including barcode)
     */
    @GET("foods/search")
    suspend fun searchFoods(
        @Query("query") query: String,
        @Query("api_key") apiKey: String,
        @Query("dataType") dataType: String = "Branded",
        @Query("pageSize") pageSize: Int = 10
    ): USDASearchResponse

    /**
     * Get detailed food information by FDC ID
     */
    @GET("food/{fdcId}")
    suspend fun getFoodById(
        @Path("fdcId") fdcId: Int,
        @Query("api_key") apiKey: String
    ): USDAFoodDetail
}

/**
 * USDA Search Response models
 */
data class USDASearchResponse(
    val foods: List<USDAFoodItem>?,
    val totalHits: Int,
    val currentPage: Int,
    val totalPages: Int
)

data class USDAFoodItem(
    val fdcId: Int,
    val description: String,
    val brandOwner: String?,
    val brandName: String?,
    val ingredients: String?,
    val servingSize: Double?,
    val servingSizeUnit: String?,
    val householdServingFullText: String?,
    val gtinUpc: String?, // This is the barcode/UPC
    val foodNutrients: List<USDANutrient>?
)

/**
 * USDA Food Detail models
 */
data class USDAFoodDetail(
    val fdcId: Int,
    val description: String,
    val brandOwner: String?,
    val brandName: String?,
    val ingredients: String?,
    val servingSize: Double?,
    val servingSizeUnit: String?,
    val householdServingFullText: String?,
    val gtinUpc: String?,
    val foodNutrients: List<USDANutrient>
)

data class USDANutrient(
    val nutrientId: Int,
    val nutrientName: String,
    val nutrientNumber: String?,
    val unitName: String,
    val value: Double?
) {
    companion object {
        // Standard nutrient IDs for USDA database
        const val ENERGY_KCAL = 1008
        const val PROTEIN = 1003
        const val TOTAL_LIPID_FAT = 1004
        const val CARBOHYDRATE = 1005
        const val FIBER = 1079
        const val SUGARS = 2000
        const val SODIUM = 1093
        const val CALCIUM = 1087
        const val IRON = 1089
        const val VITAMIN_C = 1162
    }
}
