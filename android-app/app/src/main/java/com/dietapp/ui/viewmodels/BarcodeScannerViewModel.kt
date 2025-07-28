package com.dietapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietapp.data.entities.Food
import com.dietapp.data.repository.FoodRepository
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
    val lookupError: String? = null
)

@HiltViewModel
class BarcodeScannerViewModel @Inject constructor(
    private val foodRepository: FoodRepository
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
                // Try to find in local database
                val localFood = foodRepository.getFoodByBarcode(barcode)

                if (localFood != null) {
                    _uiState.value = _uiState.value.copy(
                        isLookingUp = false,
                        foundFood = localFood
                    )
                } else {
                    // For now, create a mock food item until USDA API is implemented
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
                _uiState.value = _uiState.value.copy(
                    isLookingUp = false,
                    lookupError = "Failed to lookup food: ${e.message}"
                )
            }
        }
    }

    fun addFoodToLog(food: Food, quantity: Double = 1.0) {
        viewModelScope.launch {
            try {
                // TODO: Add to food log with user's meal selection
                // For now, just clear the found food
                _uiState.value = _uiState.value.copy(
                    foundFood = null,
                    scannedBarcode = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add food to log: ${e.message}"
                )
            }
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
}
