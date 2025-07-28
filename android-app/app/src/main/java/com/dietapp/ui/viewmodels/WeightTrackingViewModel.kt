package com.dietapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietapp.data.entities.WeightEntry
import com.dietapp.data.repository.WeightRepository
import com.dietapp.data.repository.GoalRepository
import com.dietapp.utils.UnitConverter
import com.dietapp.utils.WeightUnit
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
    private val weightRepository: WeightRepository,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeightTrackingUiState())
    val uiState: StateFlow<WeightTrackingUiState> = _uiState.asStateFlow()

    init {
        loadWeightData()
    }

    private fun loadWeightData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Get user preferences from goals
                goalRepository.getCurrentGoal("user1").collect { goal ->
                    val useImperial = goal?.useImperialUnits ?: false
                    val height = goal?.currentHeight
                    val targetWeight = goal?.targetWeight

                    // Get recent weight entries
                    weightRepository.getAllWeightEntries("user1").collect { entries ->
                        val recentEntries = entries.take(10)
                        val currentWeight = recentEntries.firstOrNull()?.weight

                        // Calculate weight change (difference between first and last entry)
                        val weightChange = if (recentEntries.size >= 2) {
                            currentWeight?.minus(recentEntries.last().weight)
                        } else null

                        _uiState.update {
                            it.copy(
                                currentWeight = currentWeight,
                                goalWeight = targetWeight,
                                weightChange = weightChange,
                                recentEntries = recentEntries,
                                useImperialUnits = useImperial,
                                currentHeight = height,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }    fun addWeightEntry(weight: Double, notes: String, unit: WeightUnit = WeightUnit.KG) {
        viewModelScope.launch {
            try {
                val weightInKg = when (unit) {
                    WeightUnit.KG -> weight
                    WeightUnit.LBS -> UnitConverter.poundsToKg(weight)
                }

                val entry = WeightEntry(
                    userId = "user1", // TODO: Get from auth
                    weight = weightInKg,
                    date = Date(),
                    notes = notes.takeIf { it.isNotBlank() }
                )

                weightRepository.insertWeightEntry(entry)
                // Data will be automatically refreshed via Flow collection
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }

    fun toggleUnits() {
        viewModelScope.launch {
            try {
                // Update user preference in goals
                val goal = goalRepository.getCurrentGoal("user1").first()
                goal?.let {
                    val updatedGoal = it.copy(useImperialUnits = !it.useImperialUnits)
                    goalRepository.updateGoal(updatedGoal)
                }
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
