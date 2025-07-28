package com.dietapp.data.models

import com.google.gson.annotations.SerializedName

data class NutritionApiResponse(
    @SerializedName("foods")
    val foods: List<FoodItem>
)

data class FoodItem(
    @SerializedName("fdcId")
    val fdcId: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("brandOwner")
    val brandOwner: String? = null,
    @SerializedName("ingredients")
    val ingredients: String? = null,
    @SerializedName("foodNutrients")
    val foodNutrients: List<FoodNutrient>,
    @SerializedName("servingSize")
    val servingSize: Double? = null,
    @SerializedName("servingSizeUnit")
    val servingSizeUnit: String? = null,
    @SerializedName("gtinUpc")
    val gtinUpc: String? = null
)

data class FoodNutrient(
    @SerializedName("nutrientId")
    val nutrientId: Int,
    @SerializedName("nutrientName")
    val nutrientName: String,
    @SerializedName("nutrientNumber")
    val nutrientNumber: String? = null,
    @SerializedName("unitName")
    val unitName: String,
    @SerializedName("value")
    val value: Double? = null
)
