package com.dietapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietapp.auth.AuthRepository
import com.dietapp.auth.AuthState
import com.dietapp.data.models.DailySummary
import com.dietapp.data.models.MotivationalTip
import com.dietapp.data.models.TipCategory
import com.dietapp.data.models.WeightTrend
import com.dietapp.data.repository.FoodRepository
import com.dietapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val userProfile: com.dietapp.data.entities.UserProfile? = null,
    val dailySummary: DailySummary? = null,
    val weightTrend: WeightTrend? = null,
    val motivationalTip: MotivationalTip? = null,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val foodRepository: FoodRepository,
    private val goalRepository: com.dietapp.data.repository.GoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authState.collect { authState ->
                when (authState) {
                    is AuthState.Authenticated -> {
                        loadDashboardData(authState.userId)
                    }
                    is AuthState.Unauthenticated -> {
                        _uiState.value = DashboardUiState(isLoading = false)
                    }
                    is AuthState.Loading -> {
                        _uiState.value = DashboardUiState(isLoading = true)
                    }
                    is AuthState.Error -> {
                        _uiState.value = DashboardUiState(
                            isLoading = false,
                            error = authState.message
                        )
                    }
                }
            }
        }
    }

    private fun loadDashboardData(userId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                // Load user profile
                val userProfile = userRepository.getUserProfile(userId)

                // If no profile exists, create a default one
                val profile = userProfile ?: createDefaultProfile(userId)

                // Check if we need to update the profile name from default "User"
                val updatedProfile = if (profile.name == "User") {
                    val actualName = authRepository.getCurrentUserFirstName()
                    if (actualName != "User") {
                        // Update the profile with the actual name
                        val newProfile = profile.copy(name = actualName, updatedAt = Date())
                        userRepository.updateUserProfile(newProfile)
                        newProfile
                    } else {
                        profile
                    }
                } else {
                    profile
                }

                // Load weight trend and motivational tip (these don't change often)
                val weightTrend = loadWeightTrend(userId)
                val motivationalTip = getMotivationalTip()

                // Observe food logs for today and update dashboard automatically
                val today = Date()
                foodRepository.getFoodLogsForDate(userId, today).collect { foodLogs ->
                    val totalCalories = foodLogs.sumOf { it.calories }
                    val totalProtein = foodLogs.sumOf { it.protein }
                    val totalCarbs = foodLogs.sumOf { it.carbs }
                    val totalFat = foodLogs.sumOf { it.fat }

                    val dailySummary = DailySummary(
                        userId = userId,
                        date = today,
                        totalCalories = totalCalories,
                        totalProtein = totalProtein,
                        totalCarbs = totalCarbs,
                        totalFat = totalFat,
                        totalCaloriesBurned = 0.0, // TODO: Implement exercise tracking
                        goalCalories = updatedProfile.dailyCalorieGoal,
                        goalProtein = updatedProfile.dailyProteinGoal,
                        goalCarbs = updatedProfile.dailyCarbGoal,
                        goalFat = updatedProfile.dailyFatGoal
                    )

                    _uiState.value = DashboardUiState(
                        isLoading = false,
                        userProfile = updatedProfile,
                        dailySummary = dailySummary,
                        weightTrend = weightTrend,
                        motivationalTip = motivationalTip
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load dashboard data"
                )
            }
        }
    }

    private suspend fun createDefaultProfile(userId: String): com.dietapp.data.entities.UserProfile {
        val defaultProfile = com.dietapp.data.entities.UserProfile(
            userId = userId,
            name = authRepository.getCurrentUserFirstName(), // Use actual user name
            email = authRepository.getCurrentUserEmail() ?: "user@example.com",
            age = 30,
            gender = "Other",
            height = 170.0,
            currentWeight = 70.0,
            targetWeight = 65.0,
            activityLevel = "Moderate",
            dietaryGoal = "Maintain Weight",
            dailyCalorieGoal = 2000,
            dailyProteinGoal = 150.0,
            dailyCarbGoal = 250.0,
            dailyFatGoal = 65.0,
            createdAt = Date(),
            updatedAt = Date()
        )

        userRepository.insertUserProfile(defaultProfile)
        return defaultProfile
    }

    private suspend fun loadWeightTrend(userId: String): WeightTrend? {
        val weightLogs = userRepository.getRecentWeightLogs(userId, 2).first()

        return if (weightLogs.isNotEmpty()) {
            val currentWeight = weightLogs[0].weight
            val previousWeight = if (weightLogs.size > 1) weightLogs[1].weight else null
            WeightTrend(currentWeight, previousWeight)
        } else {
            null
        }
    }

    private fun getMotivationalTip(): MotivationalTip {
        val tips = listOf(
            MotivationalTip(
                "tip1",
                "Stay Hydrated!",
                "Drinking water before meals can help you feel fuller and support your weight management goals.",
                TipCategory.NUTRITION
            ),
            MotivationalTip(
                "tip2",
                "Small Steps Count",
                "Every healthy choice you make today is an investment in your future self.",
                TipCategory.MOTIVATION
            ),
            MotivationalTip(
                "tip3",
                "Move More",
                "Try taking the stairs instead of the elevator. Small movements add up!",
                TipCategory.EXERCISE
            ),
            MotivationalTip(
                "tip4",
                "Mindful Eating",
                "Take time to savor your meals. Eating slowly helps with digestion and satisfaction.",
                TipCategory.HABIT
            ),
            MotivationalTip(
                "tip5",
                "Progress Not Perfection",
                "Focus on progress, not perfection. Celebrate your daily wins!",
                TipCategory.MOTIVATION
            )
        )

        return tips.random()
    }

    fun refreshData() {
        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            loadDashboardData(userId)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
