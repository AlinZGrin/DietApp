package com.dietapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietapp.auth.AuthRepository
import com.dietapp.data.entities.WaterIntake
import com.dietapp.data.repository.WaterIntakeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WaterTrackingUiState(
    val currentIntake: Double = 0.0,
    val targetIntake: Double = 2500.0, // Default 2.5L
    val hydrationStatus: String = "Very Low",
    val todaysIntakes: List<WaterIntake> = emptyList(),
    val commonServingSizes: List<Pair<String, Double>> = emptyList(),
    val showQuickAddDialog: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WaterTrackingViewModel @Inject constructor(
    private val waterIntakeRepository: WaterIntakeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WaterTrackingUiState())
    val uiState: StateFlow<WaterTrackingUiState> = _uiState.asStateFlow()

    init {
        loadCommonServingSizes()
        loadTodaysWaterIntake()
        calculateTargetIntake()
    }

    private fun loadCommonServingSizes() {
        val servingSizes = waterIntakeRepository.getCommonServingSizes()
        _uiState.value = _uiState.value.copy(commonServingSizes = servingSizes)
    }

    private fun loadTodaysWaterIntake() {
        val userId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            waterIntakeRepository.getTodaysWaterIntake(userId)
                .collect { intakes ->
                    val totalIntake = intakes.sumOf { it.amount }
                    val currentState = _uiState.value
                    val hydrationStatus = waterIntakeRepository.getHydrationStatus(
                        totalIntake,
                        currentState.targetIntake
                    )

                    _uiState.value = currentState.copy(
                        todaysIntakes = intakes,
                        currentIntake = totalIntake,
                        hydrationStatus = hydrationStatus
                    )
                }
        }
    }

    private fun calculateTargetIntake() {
        // In a real app, this would be based on user's weight and activity level
        // For now, we'll use a default value
        val defaultTarget = 2500.0 // 2.5L per day

        // TODO: Get user's weight from profile and calculate:
        // val targetIntake = waterIntakeRepository.calculateRecommendedWaterIntake(userWeight, activityLevel)

        _uiState.value = _uiState.value.copy(targetIntake = defaultTarget)
    }

    fun addWaterIntake(amount: Double) {
        val userId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            try {
                waterIntakeRepository.addWaterIntake(userId, amount)

                // Refresh the data
                loadTodaysWaterIntake()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add water intake: ${e.message}"
                )
            }
        }
    }

    fun deleteWaterIntake(intake: WaterIntake) {
        viewModelScope.launch {
            try {
                waterIntakeRepository.deleteWaterIntake(intake)

                // Refresh the data
                loadTodaysWaterIntake()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete water intake: ${e.message}"
                )
            }
        }
    }

    fun clearTodaysIntake() {
        val userId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            try {
                waterIntakeRepository.clearTodaysWaterIntake(userId)

                // Refresh the data
                loadTodaysWaterIntake()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to clear water intake: ${e.message}"
                )
            }
        }
    }

    fun showQuickAddDialog() {
        _uiState.value = _uiState.value.copy(showQuickAddDialog = true)
    }

    fun hideQuickAddDialog() {
        _uiState.value = _uiState.value.copy(showQuickAddDialog = false)
    }

    fun updateTargetIntake(target: Double) {
        val currentState = _uiState.value
        val hydrationStatus = waterIntakeRepository.getHydrationStatus(
            currentState.currentIntake,
            target
        )

        _uiState.value = currentState.copy(
            targetIntake = target,
            hydrationStatus = hydrationStatus
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
