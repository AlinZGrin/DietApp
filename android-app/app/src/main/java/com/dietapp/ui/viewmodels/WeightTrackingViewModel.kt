package com.dietapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietapp.auth.AuthRepository
import com.dietapp.auth.AuthState
import com.dietapp.data.entities.WeightEntry
import com.dietapp.data.repository.WeightRepository
import com.dietapp.data.repository.GoalRepository
import com.dietapp.debug.DatabaseDebugger
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
    private val goalRepository: GoalRepository,
    private val databaseDebugger: DatabaseDebugger
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

    // Database debug and delete functions
    fun deleteAllWeightEntries() {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUserId()
                if (userId != null) {
                    println("WeightTrackingViewModel: Starting delete for user $userId")
                    databaseDebugger.deleteAllWeightEntries(userId)
                    _uiState.update {
                        it.copy(error = "All weight entries deleted successfully")
                    }
                    println("WeightTrackingViewModel: Delete completed successfully")
                } else {
                    _uiState.update {
                        it.copy(error = "User not authenticated - cannot delete entries")
                    }
                    println("WeightTrackingViewModel: Delete failed - user not authenticated")
                }
            } catch (e: Exception) {
                println("WeightTrackingViewModel: Delete failed with error: ${e.message}")
                e.printStackTrace()
                _uiState.update {
                    it.copy(error = "Failed to delete weight entries: ${e.message}")
                }
            }
        }
    }

    fun debugDatabase() {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUserId()
                if (userId != null) {
                    println("WeightTrackingViewModel: Starting debug for user $userId")
                    databaseDebugger.printDatabaseSummary(userId)
                } else {
                    println("WeightTrackingViewModel: Debug failed - user not authenticated")
                    _uiState.update {
                        it.copy(error = "User not authenticated - cannot debug database")
                    }
                }
            } catch (e: Exception) {
                println("WeightTrackingViewModel: Debug failed with error: ${e.message}")
                e.printStackTrace()
                _uiState.update {
                    it.copy(error = "Debug failed: ${e.message}")
                }
            }
        }
    }

    fun deleteOrphanedWeightEntries() {
        viewModelScope.launch {
            try {
                // Delete entries for the old user ID that has entries (from both Room and Firestore)
                val orphanedUserId1 = "Ys7XFLPMpYQ42UOvvaQQMyuT4wC3"
                val orphanedUserId2 = "user1"

                println("WeightTrackingViewModel: Deleting orphaned entries (Room + Firestore) for multiple users")

                // Delete for first user
                println("WeightTrackingViewModel: Deleting entries for user $orphanedUserId1")
                databaseDebugger.deleteWeightEntriesForSpecificUserWithFirestore(orphanedUserId1)

                // Delete for second user
                println("WeightTrackingViewModel: Deleting entries for user $orphanedUserId2")
                databaseDebugger.deleteWeightEntriesForSpecificUserWithFirestore(orphanedUserId2)

                _uiState.update {
                    it.copy(error = "All orphaned weight entries deleted successfully")
                }
                println("WeightTrackingViewModel: All orphaned entries deleted successfully")
            } catch (e: Exception) {
                println("WeightTrackingViewModel: Failed to delete orphaned entries: ${e.message}")
                e.printStackTrace()
                _uiState.update {
                    it.copy(error = "Failed to delete orphaned entries: ${e.message}")
                }
            }
        }
    }

    fun analyzeUserIdsInDatabase() {
        viewModelScope.launch {
            try {
                println("WeightTrackingViewModel: Analyzing user IDs in database")
                databaseDebugger.showAllUserIdsInDatabase()
            } catch (e: Exception) {
                println("WeightTrackingViewModel: Failed to analyze database: ${e.message}")
                e.printStackTrace()
                _uiState.update {
                    it.copy(error = "Failed to analyze database: ${e.message}")
                }
            }
        }
    }

    fun deleteAllWeightEntriesGlobal() {
        viewModelScope.launch {
            try {
                println("WeightTrackingViewModel: Deleting ALL weight entries (Room + Firestore) globally")
                databaseDebugger.deleteAllWeightEntriesGlobalWithFirestore()
                _uiState.update {
                    it.copy(error = "All weight entries deleted globally from Room + Firestore")
                }
                println("WeightTrackingViewModel: Global deletion (Room + Firestore) completed")
            } catch (e: Exception) {
                println("WeightTrackingViewModel: Failed to delete all entries: ${e.message}")
                e.printStackTrace()
                _uiState.update {
                    it.copy(error = "Failed to delete all entries: ${e.message}")
                }
            }
        }
    }

    // 🚨 EMERGENCY: Nuclear database cleanup method
    fun nukeDatabaseEmergency() {
        viewModelScope.launch {
            try {
                println("🚨 EMERGENCY: Starting nuclear database cleanup")

                // First check how many entries we have
                val allEntries = weightRepository.getAllWeightEntriesDebug()
                println("📊 Found ${allEntries.size} total entries in Room database")

                val userIdCounts = allEntries.groupBy { it.userId }.mapValues { it.value.size }
                println("👥 User breakdown:")
                userIdCounts.forEach { (userId, count) ->
                    println("   - User $userId: $count entries")
                }

                // Delete from Firestore first
                println("🔥 Deleting from Firestore...")
                val firestoreDeleted = weightRepository.deleteAllWeightEntriesGlobalWithFirestore()
                println("✅ Deleted $firestoreDeleted entries from Firestore + Room")

                // Double-check Room deletion with direct call
                println("💾 Double-checking Room database...")
                val roomDeleted = weightRepository.deleteAllWeightEntriesGlobal()
                println("✅ Additional Room deletion: $roomDeleted entries")

                // Verify cleanup
                println("🔍 Verifying cleanup...")
                val remainingEntries = weightRepository.getAllWeightEntriesDebug()
                println("📊 Remaining entries: ${remainingEntries.size}")

                if (remainingEntries.isEmpty()) {
                    println("🎉 SUCCESS: Database completely cleaned!")
                    _uiState.update {
                        it.copy(error = "✅ SUCCESS: All ${allEntries.size} entries deleted!")
                    }
                } else {
                    println("⚠️ WARNING: ${remainingEntries.size} entries still remain")
                    remainingEntries.forEach { entry ->
                        println("   - Remaining: ${entry.weight}kg on ${entry.date} (user: ${entry.userId})")
                    }
                    _uiState.update {
                        it.copy(error = "⚠️ WARNING: ${remainingEntries.size} entries still remain")
                    }
                }

            } catch (e: Exception) {
                println("💥 ERROR: Database cleanup failed: ${e.message}")
                e.printStackTrace()
                _uiState.update {
                    it.copy(error = "💥 ERROR: Cleanup failed: ${e.message}")
                }
            }
        }
    }
}
