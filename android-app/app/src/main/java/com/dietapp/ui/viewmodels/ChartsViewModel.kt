package com.dietapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietapp.auth.AuthRepository
import com.dietapp.auth.AuthState
import com.dietapp.data.repository.FoodRepository
import com.dietapp.data.repository.UserRepository
import com.dietapp.data.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ChartsUiState(
    val isLoading: Boolean = false,
    val selectedTimePeriod: String = "Week",
    val weightData: List<Pair<String, Float>> = emptyList(),
    val calorieData: List<Pair<String, Float>> = emptyList(),
    val nutritionBreakdown: Map<String, Float> = emptyMap(),
    val averageCalories: Float = 0f,
    val averageProtein: Float = 0f,
    val averageCarbs: Float = 0f,
    val averageFat: Float = 0f,
    val weightChange: Float = 0f,
    val error: String? = null
)

@HiltViewModel
class ChartsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val foodRepository: FoodRepository,
    private val weightRepository: WeightRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChartsUiState())
    val uiState: StateFlow<ChartsUiState> = _uiState.asStateFlow()

    private val authState = authRepository.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthState.Loading
        )

    fun updateTimePeriod(period: String) {
        _uiState.value = _uiState.value.copy(selectedTimePeriod = period)
        loadChartData()
    }

    fun loadChartData() {
        viewModelScope.launch {
            authState.collect { state ->
                if (state is AuthState.Authenticated) {
                    loadData(state.userId)
                }
            }
        }
    }

    private suspend fun loadData(userId: String) {
        try {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val currentPeriod = _uiState.value.selectedTimePeriod
            val daysBack = when (currentPeriod) {
                "Week" -> 7
                "Month" -> 30
                "3 Months" -> 90
                "Year" -> 365
                else -> 7
            }

            // Load real weight data from Firebase
            val calendar = Calendar.getInstance()
            val endDate = calendar.time
            calendar.add(Calendar.DAY_OF_YEAR, -daysBack)
            val startDate = calendar.time

            val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

            // Collect weight data from repository
            var weightData: List<Pair<String, Float>> = emptyList()
            var calorieData: List<Pair<String, Float>> = emptyList()
            var nutritionBreakdown: Map<String, Float> = emptyMap()

            try {
                // Get weight entries for the period
                weightRepository.getWeightEntriesInRange(userId, startDate, endDate)
                    .collect { weightEntries ->
                        weightData = weightEntries.map { entry ->
                            val dateStr = dateFormat.format(entry.date)
                            Pair(dateStr, entry.weight.toFloat())
                        }.sortedBy { it.first }
                    }

                // Get food logs for calorie data
                foodRepository.getFoodLogsInRange(userId, startDate, endDate)
                    .collect { foodLogs ->
                        // Group by date and sum calories
                        val caloriesByDate = foodLogs.groupBy { log ->
                            dateFormat.format(log.date)
                        }.mapValues { (_, logs) ->
                            logs.sumOf { log -> log.calories }.toFloat()
                        }

                        calorieData = caloriesByDate.toList().sortedBy { it.first }

                        // Calculate nutrition breakdown from recent logs
                        val totalCalories = foodLogs.sumOf { it.calories }
                        val totalProtein = foodLogs.sumOf { it.protein }
                        val totalCarbs = foodLogs.sumOf { it.carbs }
                        val totalFat = foodLogs.sumOf { it.fat }

                        if (totalCalories > 0) {
                            nutritionBreakdown = mapOf(
                                "Protein" to (totalProtein * 4 / totalCalories * 100).toFloat(),
                                "Carbs" to (totalCarbs * 4 / totalCalories * 100).toFloat(),
                                "Fat" to (totalFat * 9 / totalCalories * 100).toFloat()
                            )
                        } else {
                            // Use default percentages if no data
                            nutritionBreakdown = mapOf(
                                "Protein" to 25f,
                                "Carbs" to 45f,
                                "Fat" to 30f
                            )
                        }
                    }
            } catch (e: Exception) {
                println("DEBUG ChartsViewModel: Error loading data from Firebase: ${e.message}")
                // Fall back to empty data or mock data
                weightData = emptyList()
                calorieData = emptyList()
                nutritionBreakdown = mapOf(
                    "Protein" to 25f,
                    "Carbs" to 45f,
                    "Fat" to 30f
                )
            }

            // Calculate averages
            val avgCalories = if (calorieData.isNotEmpty()) {
                calorieData.map { it.second }.average().toFloat()
            } else 0f

            val avgProtein = avgCalories * 0.25f / 4f // 25% of calories from protein
            val avgCarbs = avgCalories * 0.45f / 4f // 45% of calories from carbs
            val avgFat = avgCalories * 0.30f / 9f // 30% of calories from fat

            // Calculate weight change
            val weightChange = if (weightData.size >= 2) {
                weightData.last().second - weightData.first().second
            } else 0f

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                weightData = weightData,
                calorieData = calorieData,
                nutritionBreakdown = nutritionBreakdown,
                averageCalories = avgCalories,
                averageProtein = avgProtein,
                averageCarbs = avgCarbs,
                averageFat = avgFat,
                weightChange = weightChange
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Failed to load chart data"
            )
        }
    }
}
