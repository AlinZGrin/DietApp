package com.dietapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietapp.auth.AuthRepository
import com.dietapp.auth.AuthState
import com.dietapp.data.entities.WeightEntry
import com.dietapp.data.repository.WeightRepository
import com.dietapp.data.repository.GoalRepository
import com.dietapp.utils.UnitConverter
import com.dietapp.utils.WeightUnit
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class WeightTrackingUiState(
    val currentWeight: Double? = null,
    val goalWeight: Double? = null,
    val weightChange: Double? = null,
    val recentEntries: List<WeightEntry> = emptyList(),
    val useImperialUnits: Boolean = false,
    val currentHeight: Double? = null, // in cm
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WeightTrackingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val weightRepository: WeightRepository,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeightTrackingUiState())
    val uiState: StateFlow<WeightTrackingUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authState.collect { authState ->
                when (authState) {
                    is AuthState.Authenticated -> {
                        loadWeightData(authState.userId)
                    }
                    is AuthState.Unauthenticated -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Please sign in to view weight data",
                                recentEntries = emptyList()
                            )
                        }
                    }
                    is AuthState.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is AuthState.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = authState.message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadWeightData(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Use combine to react to changes in both goal and weight data
                combine(
                    goalRepository.getSimpleGoal(userId), // Use simpler goal retrieval
                    weightRepository.getAllWeightEntries(userId)
                ) { goal, entries ->
                    val useImperial = goal?.useImperialUnits ?: false
                    val height = goal?.currentHeight
                    val targetWeight = goal?.targetWeight

                    val recentEntries = entries.take(10)
                    val currentWeight = recentEntries.firstOrNull()?.weight

                    // Calculate weight change (difference between first and last entry)
                    val weightChange = if (recentEntries.size >= 2) {
                        currentWeight?.minus(recentEntries.last().weight)
                    } else null

                    WeightTrackingUiState(
                        currentWeight = currentWeight,
                        goalWeight = targetWeight,
                        weightChange = weightChange,
                        recentEntries = recentEntries,
                        useImperialUnits = useImperial,
                        currentHeight = height,
                        isLoading = false,
                        error = null
                    )
                }.collect { newState ->
                    _uiState.value = newState
                }

            } catch (e: Exception) {
                println("WeightTrackingViewModel: Error loading weight data: ${e.message}")
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun addWeightEntry(weight: Double, notes: String, unit: WeightUnit = WeightUnit.KG) {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUserId()
                if (userId == null) {
                    _uiState.update {
                        it.copy(error = "User not authenticated")
                    }
                    return@launch
                }

                val weightInKg = when (unit) {
                    WeightUnit.KG -> weight
                    WeightUnit.LBS -> UnitConverter.poundsToKg(weight)
                }

                val entry = WeightEntry(
                    userId = userId,
                    weight = weightInKg,
                    date = Date(),
                    notes = notes.takeIf { it.isNotBlank() }
                )

                weightRepository.insertWeightEntry(entry)
                // Data will be automatically refreshed via Flow collection
            } catch (e: Exception) {
                println("WeightTrackingViewModel: Error adding weight entry: ${e.message}")
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }

    fun toggleUnits() {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUserId()
                if (userId == null) {
                    _uiState.update {
                        it.copy(error = "User not authenticated")
                    }
                    return@launch
                }

                // Update user preference in goals
                // TODO: Fix this when goal updating is needed
                // val goal = goalRepository.getSimpleGoal(userId).first()
                // goal?.let {
                //     val updatedGoal = it.copy(useImperialUnits = !it.useImperialUnits)
                //     goalRepository.updateGoal(updatedGoal)
                // }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }

    fun formatWeight(weightKg: Double): String {
        val useImperial = _uiState.value.useImperialUnits
        return UnitConverter.formatWeight(weightKg, useImperial)
    }

    fun getCurrentBMI(): Double? {
        val currentWeight = _uiState.value.currentWeight
        val height = _uiState.value.currentHeight
        return if (currentWeight != null && height != null) {
            UnitConverter.calculateBMI(currentWeight, height)
        } else null
    }

    fun deleteWeightEntry(weightEntry: WeightEntry) {
        viewModelScope.launch {
            try {
                weightRepository.deleteWeightEntry(weightEntry)
                // Data will be automatically refreshed via Flow collection
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
