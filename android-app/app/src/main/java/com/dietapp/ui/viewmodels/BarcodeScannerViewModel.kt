package com.dietapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietapp.auth.AuthRepository
import com.dietapp.data.entities.Food
import com.dietapp.data.entities.FoodLog
import com.dietapp.data.repository.FoodRepository
import com.dietapp.data.repository.OpenFoodFactsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class BarcodeScannerUiState(
    val isScanning: Boolean = false,
    val scannedBarcode: String? = null,
    val error: String? = null,
    val hasPermission: Boolean = false,
    val isLookingUp: Boolean = false,
    val foundFood: Food? = null,
    val lookupError: String? = null,
    val showMealSelector: Boolean = false,
    val selectedMealType: String = "Breakfast",
    val selectedQuantity: Double = 100.0, // grams
    val isLogging: Boolean = false,
    val logSuccess: Boolean = false
)

@HiltViewModel
class BarcodeScannerViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val openFoodFactsRepository: OpenFoodFactsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BarcodeScannerUiState())
    val uiState: StateFlow<BarcodeScannerUiState> = _uiState.asStateFlow()

    fun updatePermissionStatus(hasPermission: Boolean) {
        _uiState.value = _uiState.value.copy(hasPermission = hasPermission)
    }

    fun startScanning() {
        _uiState.value = _uiState.value.copy(
            isScanning = true,
            scannedBarcode = null,
            error = null,
            foundFood = null,
            lookupError = null
        )
    }

    fun stopScanning() {
        _uiState.value = _uiState.value.copy(isScanning = false)
    }

    fun onBarcodeScanned(barcode: String) {
        _uiState.value = _uiState.value.copy(
            isScanning = false,
            scannedBarcode = barcode
        )

        // Automatically lookup the food item
        lookupFood(barcode)
    }

    fun onScanError(error: String) {
        _uiState.value = _uiState.value.copy(
            isScanning = false,
            error = error
        )
    }

    fun lookupFood(barcode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLookingUp = true,
                lookupError = null,
                foundFood = null
            )

            try {
                // First, try to find in local database
                val localFood = foodRepository.getFoodByBarcode(barcode)

                if (localFood != null) {
                    _uiState.value = _uiState.value.copy(
                        isLookingUp = false,
                        foundFood = localFood
                    )
                    return@launch
                }

                // If not found locally, search using OpenFoodFacts API
                val openFoodFactsFood = openFoodFactsRepository.searchFoodByBarcode(barcode)

                if (openFoodFactsFood != null) {
                    // Save to local database for future use
                    foodRepository.insertFood(openFoodFactsFood)

                    _uiState.value = _uiState.value.copy(
                        isLookingUp = false,
                        foundFood = openFoodFactsFood
                    )
                } else {
                    // If not found in USDA API, create a mock food item
                    val mockFood = Food(
                        id = barcode,
                        name = "Unknown Food (Barcode: $barcode)",
                        brand = "Scanned Product",
                        barcode = barcode,
                        servingSize = 100.0,
                        servingUnit = "g",
                        calories = 200.0, // Mock values
                        protein = 10.0,
                        carbs = 25.0,
                        fat = 8.0,
                        fiber = 3.0,
                        sugar = 5.0,
                        sodium = 150.0,
                        isCustom = false,
                        createdAt = Date()
                    )

                    // Save to local database for future use
                    foodRepository.insertFood(mockFood)

                    _uiState.value = _uiState.value.copy(
                        isLookingUp = false,
                        foundFood = mockFood
                    )
                }
            } catch (e: Exception) {
                println("DEBUG BarcodeScannerViewModel: Error looking up food: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLookingUp = false,
                    lookupError = "Failed to lookup food: ${e.message}"
                )
            }
        }
    }

    fun addFoodToLog(food: Food, quantity: Double = 100.0, mealType: String = "Breakfast") {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUserId()
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "User not authenticated"
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(isLogging = true)

                // Calculate nutrition values based on quantity (per 100g)
                val quantityMultiplier = quantity / 100.0
                val calculatedCalories = food.calories * quantityMultiplier
                val calculatedProtein = food.protein * quantityMultiplier
                val calculatedCarbs = food.carbs * quantityMultiplier
                val calculatedFat = food.fat * quantityMultiplier

                val foodLog = FoodLog(
                    userId = userId,
                    foodId = food.id,
                    quantity = quantity,
                    unit = "g",
                    mealType = mealType,
                    date = Date(),
                    calories = calculatedCalories,
                    protein = calculatedProtein,
                    carbs = calculatedCarbs,
                    fat = calculatedFat,
                    createdAt = Date()
                )

                foodRepository.insertFoodLog(foodLog)

                _uiState.value = _uiState.value.copy(
                    isLogging = false,
                    logSuccess = true,
                    showMealSelector = false,
                    foundFood = null,
                    scannedBarcode = null
                )

                println("DEBUG BarcodeScannerViewModel: Successfully logged food: ${food.name}, quantity: ${quantity}g, calories: ${calculatedCalories}")

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLogging = false,
                    error = "Failed to add food to log: ${e.message}"
                )
                println("DEBUG BarcodeScannerViewModel: Error logging food: ${e.message}")
            }
        }
    }

    fun showMealSelector() {
        _uiState.value = _uiState.value.copy(showMealSelector = true)
    }

    fun hideMealSelector() {
        _uiState.value = _uiState.value.copy(
            showMealSelector = false,
            logSuccess = false
        )
    }

    fun setMealType(mealType: String) {
        _uiState.value = _uiState.value.copy(selectedMealType = mealType)
    }

    fun setQuantity(quantity: Double) {
        _uiState.value = _uiState.value.copy(selectedQuantity = quantity)
    }

    fun addSelectedFoodToLog() {
        val currentState = _uiState.value
        currentState.foundFood?.let { food ->
            addFoodToLog(
                food = food,
                quantity = currentState.selectedQuantity,
                mealType = currentState.selectedMealType
            )
        }
    }

    fun clearScannedBarcode() {
        _uiState.value = _uiState.value.copy(
            scannedBarcode = null,
            foundFood = null,
            lookupError = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearLookupError() {
        _uiState.value = _uiState.value.copy(lookupError = null)
    }

    fun updateError(error: String) {
        _uiState.value = _uiState.value.copy(error = error)
    }
}
