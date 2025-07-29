package com.dietapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietapp.auth.AuthRepository
import com.dietapp.auth.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val email: String = "",
    val password: String = "",
    val isSignUpMode: Boolean = false,
    val showForgotPassword: Boolean = false,
    val isSendingPasswordReset: Boolean = false,
    val passwordResetSent: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val authState: StateFlow<AuthState> = authRepository.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthState.Loading
        )

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun toggleSignUpMode() {
        _uiState.value = _uiState.value.copy(
            isSignUpMode = !_uiState.value.isSignUpMode,
            error = null,
            showForgotPassword = false,
            passwordResetSent = false
        )
    }

    fun showForgotPassword() {
        _uiState.value = _uiState.value.copy(
            showForgotPassword = true,
            error = null,
            passwordResetSent = false
        )
    }

    fun hideForgotPassword() {
        _uiState.value = _uiState.value.copy(
            showForgotPassword = false,
            error = null,
            passwordResetSent = false
        )
    }

    fun sendPasswordResetEmail() {
        val currentState = _uiState.value
        if (currentState.email.isBlank()) {
            _uiState.value = currentState.copy(error = "Please enter your email address")
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isSendingPasswordReset = true, error = null)

            val result = authRepository.sendPasswordResetEmail(currentState.email)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSendingPasswordReset = false,
                        passwordResetSent = true,
                        error = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSendingPasswordReset = false,
                        error = error.message ?: "Failed to send password reset email"
                    )
                }
            )
        }
    }

    fun signIn() {
        val currentState = _uiState.value
        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _uiState.value = currentState.copy(error = "Email and password are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)

            val result = if (currentState.isSignUpMode) {
                authRepository.signUpWithEmail(currentState.email, currentState.password)
            } else {
                authRepository.signInWithEmail(currentState.email, currentState.password)
            }

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Authentication failed"
                    )
                }
            )
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = authRepository.signInWithGoogle()

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Google sign-in failed"
                    )
                }
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearPasswordResetSent() {
        _uiState.value = _uiState.value.copy(passwordResetSent = false)
    }
}
