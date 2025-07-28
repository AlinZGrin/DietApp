package com.dietapp.ui.screens.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietapp.data.dao.FoodDao
import com.dietapp.data.entities.Food
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BarcodeScannerViewModel @Inject constructor(
    private val foodDao: FoodDao
) : ViewModel() {

    // USDA FoodData Central API configuration
    companion object {
        // USDA API key for accessing FoodData Central API
        private const val USDA_API_KEY = "fCnWsoZ4D22bLKBaEOrmTfGpYhYCV49MWWWGxeHt"
        private const val USDA_BASE_URL = "https://api.nal.usda.gov/fdc/v1"
    }

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private val _scannedFoodItem = MutableStateFlow<Food?>(null)
    val scannedFoodItem: StateFlow<Food?> = _scannedFoodItem.asStateFlow()

    fun onBarcodeScanned(barcode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // First check local database
                val localFood = foodDao.getFoodByBarcode(barcode)

                if (localFood != null) {
                    _scannedFoodItem.value = localFood
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        foundFood = true
                    )
                } else {
                    // In a real app, you'd call an API like OpenFoodFacts here
                    // For now, we'll simulate an API call
                    lookupFoodByBarcode(barcode)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error scanning barcode: ${e.message}"
                )
            }
        }
    }

    private suspend fun lookupFoodByBarcode(barcode: String) {
        try {
            // First, search for the food by barcode using USDA API
            val searchResponse = withContext(Dispatchers.IO) {
                val searchUrl = "$USDA_BASE_URL/foods/search?api_key=$USDA_API_KEY&query=$barcode&dataType=Branded"
                val client = okhttp3.OkHttpClient()
                val request = okhttp3.Request.Builder()
                    .url(searchUrl)
                    .build()

                client.newCall(request).execute()
            }

            if (searchResponse.isSuccessful) {
                val searchBody = searchResponse.body?.string()

                // Parse search results to find exact barcode match
                val fdcId = extractFdcIdFromSearch(searchBody, barcode)

                if (fdcId != null) {
                    // Get detailed food information using FDC ID
                    val detailResponse = withContext(Dispatchers.IO) {
                        val detailUrl = "$USDA_BASE_URL/food/$fdcId?api_key=$USDA_API_KEY"
                        val client = okhttp3.OkHttpClient()
                        val request = okhttp3.Request.Builder()
                            .url(detailUrl)
                            .build()

                        client.newCall(request).execute()
                    }

                    if (detailResponse.isSuccessful) {
                        val detailBody = detailResponse.body?.string()
                        parseUSDAFoodDetails(detailBody, barcode)
                    } else {
                        createUnknownProduct(barcode)
                    }
                } else {
                    // No exact match found, try broader search or create unknown product
                    tryBroaderSearch(barcode)
                }
            } else {
                // API call failed, create a basic entry
                createUnknownProduct(barcode)
            }
        } catch (e: Exception) {
            println("DEBUG BarcodeScannerViewModel: Error looking up barcode with USDA API: ${e.message}")
            // Fallback to creating an unknown product
            createUnknownProduct(barcode)
        }
    }

    private fun extractFdcIdFromSearch(searchResponse: String?, targetBarcode: String): String? {
        return try {
            // Look for foods array and find exact barcode match
            // This is a simplified JSON parsing - in production you'd use Gson/Moshi
            val foodsStart = searchResponse?.indexOf("\"foods\":[")
            if (foodsStart != null && foodsStart >= 0) {
                val foodsSection = searchResponse.substring(foodsStart)

                // Find food entries that contain our barcode
                val barcodePattern = "\"gtinUpc\":\"$targetBarcode\""
                val barcodeIndex = foodsSection.indexOf(barcodePattern)

                if (barcodeIndex >= 0) {
                    // Find the fdcId for this food item
                    val foodItemStart = foodsSection.lastIndexOf("{", barcodeIndex)
                    val foodItemEnd = foodsSection.indexOf("}", barcodeIndex)

                    if (foodItemStart >= 0 && foodItemEnd >= 0) {
                        val foodItem = foodsSection.substring(foodItemStart, foodItemEnd)
                        return extractJsonValue(foodItem, "fdcId")
                    }
                }
            }
            null
        } catch (e: Exception) {
            println("DEBUG: Error extracting FDC ID: ${e.message}")
            null
        }
    }

    private suspend fun parseUSDAFoodDetails(detailResponse: String?, barcode: String) {
        try {
            if (detailResponse == null) {
                createUnknownProduct(barcode)
                return
            }

            // Extract basic food information
            val description = extractJsonValue(detailResponse, "description") ?: "Unknown Product"
            val brandOwner = extractJsonValue(detailResponse, "brandOwner") ?: "Unknown Brand"

            // Extract nutrition information from foodNutrients array
            var calories = 0.0
            var protein = 0.0
            var carbs = 0.0
            var fat = 0.0

            // Parse nutrients (simplified - you'd want more robust JSON parsing)
            val nutrientsStart = detailResponse.indexOf("\"foodNutrients\":[")
            if (nutrientsStart >= 0) {
                val nutrientsSection = detailResponse.substring(nutrientsStart)

                // Energy (calories) - nutrient ID 1008
                calories = extractNutrientValue(nutrientsSection, "1008")?.toDouble() ?: 0.0

                // Protein - nutrient ID 1003
                protein = extractNutrientValue(nutrientsSection, "1003")?.toDouble() ?: 0.0

                // Carbohydrates - nutrient ID 1005
                carbs = extractNutrientValue(nutrientsSection, "1005")?.toDouble() ?: 0.0

                // Total fat - nutrient ID 1004
                fat = extractNutrientValue(nutrientsSection, "1004")?.toDouble() ?: 0.0
            }

            val food = Food(
                id = barcode,
                name = description,
                brand = brandOwner,
                barcode = barcode,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                servingSize = 100.0, // USDA data is typically per 100g
                servingUnit = "g",
                isCustom = false
            )

            // Save to local database
            foodDao.insertFood(food)

            _scannedFoodItem.value = food
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                foundFood = true
            )

        } catch (e: Exception) {
            println("DEBUG BarcodeScannerViewModel: Error parsing USDA food details: ${e.message}")
            createUnknownProduct(barcode)
        }
    }

    private fun extractNutrientValue(nutrientsSection: String, nutrientId: String): Float? {
        return try {
            // Find nutrient with specific ID
            val pattern = "\"nutrient\":\\s*\\{[^}]*\"id\":\\s*$nutrientId[^}]*\\}"
            val nutrientMatch = Regex(pattern).find(nutrientsSection)

            if (nutrientMatch != null) {
                val nutrientBlock = nutrientsSection.substring(
                    nutrientMatch.range.first,
                    nutrientsSection.indexOf("}", nutrientMatch.range.last) + 1
                )

                // Extract the amount value
                val amountPattern = "\"amount\":\\s*([0-9.]+)"
                val amountMatch = Regex(amountPattern).find(nutrientBlock)
                return amountMatch?.groupValues?.get(1)?.toFloatOrNull()
            }
            null
        } catch (e: Exception) {
            println("DEBUG: Error extracting nutrient $nutrientId: ${e.message}")
            null
        }
    }

    private suspend fun tryBroaderSearch(barcode: String) {
        try {
            // Try a broader search without exact barcode matching
            val searchResponse = withContext(Dispatchers.IO) {
                val searchUrl = "$USDA_BASE_URL/foods/search?api_key=$USDA_API_KEY&query=${barcode.take(8)}&dataType=Branded&pageSize=5"
                val client = okhttp3.OkHttpClient()
                val request = okhttp3.Request.Builder()
                    .url(searchUrl)
                    .build()

                client.newCall(request).execute()
            }

            if (searchResponse.isSuccessful) {
                val searchBody = searchResponse.body?.string()
                // Try to get the first result if any
                val fdcId = extractFirstFdcId(searchBody)

                if (fdcId != null) {
                    val detailResponse = withContext(Dispatchers.IO) {
                        val detailUrl = "$USDA_BASE_URL/food/$fdcId?api_key=$USDA_API_KEY"
                        val client = okhttp3.OkHttpClient()
                        val request = okhttp3.Request.Builder()
                            .url(detailUrl)
                            .build()

                        client.newCall(request).execute()
                    }

                    if (detailResponse.isSuccessful) {
                        val detailBody = detailResponse.body?.string()
                        parseUSDAFoodDetails(detailBody, barcode)
                        return
                    }
                }
            }

            // If broader search fails, create unknown product
            createUnknownProduct(barcode)

        } catch (e: Exception) {
            println("DEBUG BarcodeScannerViewModel: Error in broader search: ${e.message}")
            createUnknownProduct(barcode)
        }
    }

    private fun extractFirstFdcId(searchResponse: String?): String? {
        return try {
            val foodsStart = searchResponse?.indexOf("\"foods\":[")
            if (foodsStart != null && foodsStart >= 0) {
                val foodsSection = searchResponse.substring(foodsStart)
                val firstFoodStart = foodsSection.indexOf("{")
                val firstFoodEnd = foodsSection.indexOf("}", firstFoodStart)

                if (firstFoodStart >= 0 && firstFoodEnd >= 0) {
                    val firstFood = foodsSection.substring(firstFoodStart, firstFoodEnd)
                    return extractJsonValue(firstFood, "fdcId")
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun extractJsonValue(json: String, key: String): String? {
        return try {
            val pattern = "\"$key\"\\s*:\\s*\"([^\"]*)\""
            val regex = Regex(pattern)
            regex.find(json)?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun createUnknownProduct(barcode: String) {
        val mockFoodItem = Food(
            id = barcode,
            name = "Unknown Product $barcode",
            brand = "Unknown Brand",
            calories = 250.0,
            protein = 10.0,
            carbs = 30.0,
            fat = 8.0,
            barcode = barcode,
            servingSize = 100.0,
            servingUnit = "g",
            isCustom = true
        )

        // Save to local database
        foodDao.insertFood(mockFoodItem)

        _scannedFoodItem.value = mockFoodItem
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            foundFood = true
        )
    }

    fun resetScanner() {
        _uiState.value = ScannerUiState()
        _scannedFoodItem.value = null
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class ScannerUiState(
    val isLoading: Boolean = false,
    val foundFood: Boolean = false,
    val errorMessage: String? = null
)
