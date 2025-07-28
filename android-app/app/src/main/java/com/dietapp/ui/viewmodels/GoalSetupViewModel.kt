package com.dietapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietapp.auth.AuthRepository
import com.dietapp.data.entities.Goal
import com.dietapp.data.repository.GoalRepository
import com.dietapp.utils.UnitConverter
import com.dietapp.utils.WeightUnit
import com.dietapp.utils.HeightUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class GoalSetupUiState(
    val currentWeight: String = "",
    val targetWeight: String = "",
    val height: String = "",
    val heightFeet: String = "",
    val heightInches: String = "",
    val age: String = "",
    val gender: String = "Male",
    val goalType: String = "weight_loss",
    val activityLevel: String = "moderate",
    val weeklyWeightGoal: String = "0.5",
    val useImperialUnits: Boolean = false,
    val calculatedBMR: Double = 0.0,
    val calculatedTDEE: Double = 0.0,
    val calculatedCalories: Double = 0.0,
    val calculatedProtein: Double = 0.0,
    val calculatedCarbs: Double = 0.0,
    val calculatedFat: Double = 0.0,
    val isValid: Boolean = false,
    val isLoading: Boolean = false,
    val isGoalCreated: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GoalSetupViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalSetupUiState())
    val uiState: StateFlow<GoalSetupUiState> = _uiState.asStateFlow()

    fun updateCurrentWeight(weight: String) {
        _uiState.value = _uiState.value.copy(currentWeight = weight)
        calculateTargets()
        validateForm()
    }

    fun updateTargetWeight(weight: String) {
        _uiState.value = _uiState.value.copy(targetWeight = weight)
        calculateTargets()
        validateForm()
    }

    fun updateHeight(height: String) {
        _uiState.value = _uiState.value.copy(height = height)
        calculateTargets()
        validateForm()
    }

    fun updateHeightFeet(feet: String) {
        _uiState.value = _uiState.value.copy(heightFeet = feet)
        calculateTargets()
        validateForm()
    }

    fun updateHeightInches(inches: String) {
        _uiState.value = _uiState.value.copy(heightInches = inches)
        calculateTargets()
        validateForm()
    }

    fun toggleUnits() {
        val currentState = _uiState.value
        val newUseImperial = !currentState.useImperialUnits

        if (newUseImperial) {
            // Convert from metric to imperial
            val weightKg = currentState.currentWeight.toDoubleOrNull()
            val targetWeightKg = currentState.targetWeight.toDoubleOrNull()
            val heightCm = currentState.height.toDoubleOrNull()
            val weeklyGoalKg = currentState.weeklyWeightGoal.toDoubleOrNull()

            val (feet, inches) = if (heightCm != null) {
                UnitConverter.cmToFeetInches(heightCm)
            } else {
                Pair(0, 0)
            }

            _uiState.value = currentState.copy(
                useImperialUnits = true,
                currentWeight = weightKg?.let { UnitConverter.kgToPounds(it).toString() } ?: "",
                targetWeight = targetWeightKg?.let { UnitConverter.kgToPounds(it).toString() } ?: "",
                weeklyWeightGoal = weeklyGoalKg?.let { UnitConverter.kgToPounds(it).toString() } ?: "",
                heightFeet = feet.toString(),
                heightInches = inches.toString()
            )
        } else {
            // Convert from imperial to metric
            val weightLbs = currentState.currentWeight.toDoubleOrNull()
            val targetWeightLbs = currentState.targetWeight.toDoubleOrNull()
            val weeklyGoalLbs = currentState.weeklyWeightGoal.toDoubleOrNull()
            val feet = currentState.heightFeet.toIntOrNull() ?: 0
            val inches = currentState.heightInches.toIntOrNull() ?: 0

            _uiState.value = currentState.copy(
                useImperialUnits = false,
                currentWeight = weightLbs?.let { UnitConverter.poundsToKg(it).toString() } ?: "",
                targetWeight = targetWeightLbs?.let { UnitConverter.poundsToKg(it).toString() } ?: "",
                weeklyWeightGoal = weeklyGoalLbs?.let { UnitConverter.poundsToKg(it).toString() } ?: "",
                height = UnitConverter.feetInchesToCm(feet, inches).toString()
            )
        }

        calculateTargets()
        validateForm()
    }

    fun updateAge(age: String) {
        _uiState.value = _uiState.value.copy(age = age)
        calculateTargets()
        validateForm()
    }

    fun updateGender(gender: String) {
        _uiState.value = _uiState.value.copy(gender = gender)
        calculateTargets()
        validateForm()
    }

    fun updateGoalType(goalType: String) {
        _uiState.value = _uiState.value.copy(goalType = goalType)
        calculateTargets()
        validateForm()
    }

    fun updateActivityLevel(activityLevel: String) {
        _uiState.value = _uiState.value.copy(activityLevel = activityLevel)
        calculateTargets()
        validateForm()
    }

    fun updateWeeklyWeightGoal(goal: String) {
        _uiState.value = _uiState.value.copy(weeklyWeightGoal = goal)
        calculateTargets()
        validateForm()
    }

    private fun calculateTargets() {
        val state = _uiState.value

        try {
            // Get weight in kg
            val weightKg = if (state.useImperialUnits) {
                state.currentWeight.toDoubleOrNull()?.let { UnitConverter.poundsToKg(it) }
            } else {
                state.currentWeight.toDoubleOrNull()
            } ?: return

            // Get height in cm
            val heightCm = if (state.useImperialUnits) {
                val feet = state.heightFeet.toIntOrNull() ?: return
                val inches = state.heightInches.toIntOrNull() ?: return
                UnitConverter.feetInchesToCm(feet, inches)
            } else {
                state.height.toDoubleOrNull() ?: return
            }

            val age = state.age.toIntOrNull() ?: return

            // Get weekly goal in kg
            val weeklyGoalKg = if (state.useImperialUnits) {
                state.weeklyWeightGoal.toDoubleOrNull()?.let { UnitConverter.poundsToKg(it) }
            } else {
                state.weeklyWeightGoal.toDoubleOrNull()
            } ?: return

            // Calculate BMR using metric values
            val bmr = goalRepository.calculateBMR(
                weight = weightKg,
                height = heightCm,
                age = age,
                isMale = state.gender == "Male"
            )

            // Calculate TDEE
            val tdee = goalRepository.calculateTDEE(bmr, state.activityLevel)

            // Calculate target calories
            val targetCalories = goalRepository.calculateTargetCalories(
                tdee = tdee,
                goalType = state.goalType,
                weeklyWeightChangeGoal = if (state.goalType == "weight_loss") -weeklyGoalKg else weeklyGoalKg
            )

            // Calculate macro targets
            val (protein, carbs, fat) = goalRepository.calculateMacroTargets(targetCalories, state.goalType)

            _uiState.value = state.copy(
                calculatedBMR = bmr,
                calculatedTDEE = tdee,
                calculatedCalories = targetCalories,
                calculatedProtein = protein,
                calculatedCarbs = carbs,
                calculatedFat = fat
            )
        } catch (e: Exception) {
            // Invalid input, keep current values
        }
    }

    private fun validateForm() {
        val state = _uiState.value

        // Check height based on unit system
        val hasValidHeight = if (state.useImperialUnits) {
            state.heightFeet.toIntOrNull() != null && state.heightInches.toIntOrNull() != null
        } else {
            state.height.toDoubleOrNull() != null
        }

        val isValid = state.currentWeight.toDoubleOrNull() != null &&
                state.targetWeight.toDoubleOrNull() != null &&
                hasValidHeight &&
                state.age.toIntOrNull() != null &&
                state.weeklyWeightGoal.toDoubleOrNull() != null &&
                state.calculatedCalories > 0

        _uiState.value = state.copy(isValid = isValid)
    }

    fun createGoal() {
        val state = _uiState.value
        val userId = authRepository.getCurrentUserId()

        println("DEBUG ViewModel: Creating goal - isValid: ${state.isValid}, userId: $userId")

        if (!state.isValid || userId == null) {
            val errorMsg = if (!state.isValid) "Please fill all required fields correctly" else "User not authenticated"
            println("DEBUG ViewModel: Validation failed - $errorMsg")
            _uiState.value = state.copy(error = errorMsg)
            return
        }

        _uiState.value = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // Convert values to metric for storage
                val targetWeightKg = if (state.useImperialUnits) {
                    UnitConverter.poundsToKg(state.targetWeight.toDouble())
                } else {
                    state.targetWeight.toDouble()
                }

                val currentWeightKg = if (state.useImperialUnits) {
                    UnitConverter.poundsToKg(state.currentWeight.toDouble())
                } else {
                    state.currentWeight.toDouble()
                }

                val heightCm = if (state.useImperialUnits) {
                    val feet = state.heightFeet.toInt()
                    val inches = state.heightInches.toInt()
                    UnitConverter.feetInchesToCm(feet, inches)
                } else {
                    state.height.toDouble()
                }

                val weeklyGoalKg = if (state.useImperialUnits) {
                    UnitConverter.poundsToKg(state.weeklyWeightGoal.toDouble())
                } else {
                    state.weeklyWeightGoal.toDouble()
                }

                // Calculate target date based on current and target weight
                val weightDifference = Math.abs(targetWeightKg - currentWeightKg)
                val weeksToGoal = (weightDifference / weeklyGoalKg).toLong()

                val calendar = Calendar.getInstance()
                calendar.add(Calendar.WEEK_OF_YEAR, weeksToGoal.toInt())
                val targetDate = calendar.time

                val goal = Goal(
                    userId = userId,
                    goalType = state.goalType,
                    targetWeight = targetWeightKg,
                    targetCalories = state.calculatedCalories,
                    targetProtein = state.calculatedProtein,
                    targetCarbs = state.calculatedCarbs,
                    targetFat = state.calculatedFat,
                    targetDate = targetDate,
                    weeklyWeightLossGoal = if (state.goalType == "weight_loss") weeklyGoalKg else -weeklyGoalKg,
                    activityLevel = state.activityLevel,
                    currentHeight = heightCm,
                    useImperialUnits = state.useImperialUnits,
                    isActive = true
                )

                println("DEBUG ViewModel: Calling goalRepository.createNewGoal")
                val goalId = goalRepository.createNewGoal(goal)
                println("DEBUG ViewModel: Goal created successfully with ID: $goalId")

                _uiState.value = state.copy(
                    isLoading = false,
                    isGoalCreated = true
                )

            } catch (e: Exception) {
                println("DEBUG ViewModel: Error creating goal: ${e.message}")
                e.printStackTrace()
                _uiState.value = state.copy(
                    isLoading = false,
                    error = "Failed to create goal: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
