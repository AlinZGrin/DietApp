package com.dietapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietapp.auth.AuthRepository
import com.dietapp.data.entities.Food
import com.dietapp.data.entities.FoodLog
import com.dietapp.data.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class FoodLogWithFood(
    val foodLog: FoodLog,
    val food: Food?
)

data class FoodLoggingUiState(
    val selectedDate: Date = Date(),
    val foodLogs: List<FoodLog> = emptyList(),
    val foodLogsWithFood: List<FoodLogWithFood> = emptyList(),
    val groupedLogs: Map<String, List<FoodLogWithFood>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddFoodDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val editingFoodLog: FoodLogWithFood? = null,
    val selectedMealType: String = "Breakfast",
    val totalCalories: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0
)

@HiltViewModel
class FoodLoggingViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodLoggingUiState())
    val uiState: StateFlow<FoodLoggingUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        loadFoodLogsForSelectedDate()
    }

    fun loadFoodLogsForSelectedDate() {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _uiState.value = _uiState.value.copy(error = "User not authenticated")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                foodRepository.getFoodLogsForDate(userId, _uiState.value.selectedDate)
                    .collect { logs ->
                        // Fetch food information for each log
                        val foodLogsWithFood = logs.map { foodLog ->
                            val food = try {
                                foodRepository.getFoodById(foodLog.foodId)
                            } catch (e: Exception) {
                                null
                            }
                            FoodLogWithFood(foodLog, food)
                        }

                        val groupedLogs = foodLogsWithFood.groupBy { it.foodLog.mealType }
                        val totalCalories = logs.sumOf { it.calories }
                        val totalProtein = logs.sumOf { it.protein }
                        val totalCarbs = logs.sumOf { it.carbs }
                        val totalFat = logs.sumOf { it.fat }

                        _uiState.value = _uiState.value.copy(
                            foodLogs = logs,
                            foodLogsWithFood = foodLogsWithFood,
                            groupedLogs = groupedLogs,
                            totalCalories = totalCalories,
                            totalProtein = totalProtein,
                            totalCarbs = totalCarbs,
                            totalFat = totalFat,
                            isLoading = false,
                            error = null
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load food logs: ${e.message}"
                )
            }
        }
    }

    fun setSelectedDate(date: Date) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        loadFoodLogsForSelectedDate()
    }

    fun showAddFoodDialog(mealType: String = "Breakfast") {
        _uiState.value = _uiState.value.copy(
            showAddFoodDialog = true,
            selectedMealType = mealType
        )
    }

    fun hideAddFoodDialog() {
        _uiState.value = _uiState.value.copy(showAddFoodDialog = false)
    }

    fun showEditDialog(foodLogWithFood: FoodLogWithFood) {
        _uiState.value = _uiState.value.copy(
            showEditDialog = true,
            editingFoodLog = foodLogWithFood
        )
    }

    fun hideEditDialog() {
        _uiState.value = _uiState.value.copy(
            showEditDialog = false,
            editingFoodLog = null
        )
    }

    fun deleteFoodLog(foodLogWithFood: FoodLogWithFood) {
        viewModelScope.launch {
            try {
                foodRepository.deleteFoodLog(foodLogWithFood.foodLog)
                // Food logs will be automatically updated through the Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete food log: ${e.message}"
                )
            }
        }
    }

    fun updateFoodLog(
        foodLogWithFood: FoodLogWithFood,
        newQuantity: Double,
        newMealType: String
    ) {
        viewModelScope.launch {
            try {
                // Use the existing food information or fetch it
                val originalFood = foodLogWithFood.food ?: foodRepository.getFoodById(foodLogWithFood.foodLog.foodId)
                if (originalFood != null) {
                    val quantityMultiplier = newQuantity / 100.0
                    val updatedFoodLog = foodLogWithFood.foodLog.copy(
                        quantity = newQuantity,
                        mealType = newMealType,
                        calories = originalFood.calories * quantityMultiplier,
                        protein = originalFood.protein * quantityMultiplier,
                        carbs = originalFood.carbs * quantityMultiplier,
                        fat = originalFood.fat * quantityMultiplier
                    )

                    foodRepository.updateFoodLog(updatedFoodLog)
                    hideEditDialog()
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Could not find original food information"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update food log: ${e.message}"
                )
            }
        }
    }

    fun addFoodToLog(food: Food, mealType: String, quantity: Double = 100.0) {
        viewModelScope.launch {
            try {
                // First, save the food to the database if it doesn't exist
                // This ensures the food is available when we try to display food logs
                val existingFood = foodRepository.getFoodById(food.id)
                if (existingFood == null) {
                    foodRepository.insertFood(food)
                }

                // Calculate nutrition values based on quantity
                val factor = quantity / 100.0 // Food values are per 100g

                val foodLog = FoodLog(
                    id = 0, // Auto-generated
                    userId = authRepository.getCurrentUserId() ?: "",
                    foodId = food.id,
                    quantity = quantity,
                    unit = "g",
                    mealType = mealType,
                    date = _uiState.value.selectedDate,
                    calories = food.calories * factor,
                    protein = food.protein * factor,
                    carbs = food.carbs * factor,
                    fat = food.fat * factor
                )

                foodRepository.insertFoodLog(foodLog)
                hideAddFoodDialog()

                // Food logs will be automatically updated through the Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add food to log: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun goToPreviousDay() {
        val calendar = Calendar.getInstance()
        calendar.time = _uiState.value.selectedDate
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        setSelectedDate(calendar.time)
    }

    fun goToNextDay() {
        val calendar = Calendar.getInstance()
        calendar.time = _uiState.value.selectedDate
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        setSelectedDate(calendar.time)
    }

    fun goToToday() {
        setSelectedDate(Date())
    }

    fun getDateString(): String {
        return dateFormat.format(_uiState.value.selectedDate)
    }

    fun isToday(): Boolean {
        return dateFormat.format(_uiState.value.selectedDate) == dateFormat.format(Date())
    }
}
