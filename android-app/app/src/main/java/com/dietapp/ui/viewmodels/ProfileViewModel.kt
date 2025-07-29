package com.dietapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietapp.auth.AuthRepository
import com.dietapp.auth.AuthState
import com.dietapp.data.entities.UserProfile
import com.dietapp.data.repository.UserRepository
import com.dietapp.data.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val userProfile: UserProfile? = null,
    val currentWeight: Double? = null, // Latest weight from weight entries
    val isEditing: Boolean = false,
    val error: String? = null,
    val isSigningOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val weightRepository: WeightRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authState.collect { authState ->
                when (authState) {
                    is AuthState.Authenticated -> {
                        loadUserProfile(authState.userId)
                    }
                    is AuthState.Unauthenticated -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                userProfile = null,
                                error = "Please sign in to view profile"
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

    private fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // Load both user profile and latest weight in parallel
                combine(
                    userRepository.getUserProfileFlow(userId),
                    weightRepository.getLatestWeightEntry(userId)
                ) { profile, latestWeight ->
                    Pair(profile, latestWeight)
                }.collect { (profile, latestWeight) ->
                    if (profile != null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                userProfile = profile,
                                currentWeight = latestWeight?.weight, // Use latest weight entry
                                error = null
                            )
                        }
                    } else {
                        // Create default profile if none exists
                        createDefaultProfile(userId)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load profile"
                    )
                }
            }
        }
    }

    private suspend fun createDefaultProfile(userId: String) {
        try {
            val defaultProfile = UserProfile(
                userId = userId,
                name = authRepository.getCurrentUserFirstName(),
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
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Failed to create profile: ${e.message}"
                )
            }
        }
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditing = !it.isEditing) }
    }

    fun updateProfile(
        name: String,
        age: Int,
        gender: String,
        height: Double,
        targetWeight: Double,
        activityLevel: String,
        dietaryGoal: String
    ) {
        viewModelScope.launch {
            try {
                val currentProfile = _uiState.value.userProfile
                if (currentProfile != null) {
                    val updatedProfile = currentProfile.copy(
                        name = name,
                        age = age,
                        gender = gender,
                        height = height,
                        targetWeight = targetWeight,
                        activityLevel = activityLevel,
                        dietaryGoal = dietaryGoal,
                        updatedAt = Date()
                    )

                    userRepository.updateUserProfile(updatedProfile)
                    _uiState.update { it.copy(isEditing = false) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to update profile: ${e.message}")
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSigningOut = true) }
                authRepository.signOut()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSigningOut = false,
                        error = "Failed to sign out: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun getCurrentUserEmail(): String? {
        return authRepository.getCurrentUserEmail()
    }

    fun getCurrentUserDisplayName(): String? {
        return authRepository.getCurrentUserDisplayName()
    }
}
