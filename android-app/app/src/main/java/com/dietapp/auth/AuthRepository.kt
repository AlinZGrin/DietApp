package com.dietapp.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val userId: String, val email: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

@Singleton
class AuthRepository @Inject constructor() {
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: Flow<AuthState> = _authState.asStateFlow()

    init {
        // Observe Firebase auth state changes
        observeAuthState()
    }

    private fun observeAuthState() {
        firebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                _authState.value = AuthState.Authenticated(user.uid, user.email ?: "")
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<String> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                _authState.value = AuthState.Authenticated(user.uid, user.email ?: "")
                Result.success(user.uid)
            } else {
                _authState.value = AuthState.Error("Authentication failed")
                Result.failure(Exception("Authentication failed"))
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Authentication failed")
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, password: String): Result<String> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                _authState.value = AuthState.Authenticated(user.uid, user.email ?: "")
                Result.success(user.uid)
            } else {
                _authState.value = AuthState.Error("Sign up failed")
                Result.failure(Exception("Sign up failed"))
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Sign up failed")
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(): Result<String> {
        return try {
            // Note: Google Sign-In requires additional setup with Google Play Services
            // For now, returning an error to indicate it's not implemented
            _authState.value = AuthState.Error("Google sign-in not yet implemented")
            Result.failure(Exception("Google sign-in not yet implemented"))
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Google sign-in failed")
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        try {
            firebaseAuth.signOut()
            _authState.value = AuthState.Unauthenticated
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Sign out failed")
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    fun getCurrentUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }

    fun isSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    fun getCurrentUserDisplayName(): String? {
        return firebaseAuth.currentUser?.displayName
    }

    fun getCurrentUserFirstName(): String {
        val user = firebaseAuth.currentUser
        // Try display name first
        user?.displayName?.let { displayName ->
            // If display name exists, try to extract first name
            val firstName = displayName.split(" ").firstOrNull()
            if (!firstName.isNullOrBlank()) {
                return firstName
            }
        }

        // Fallback to email username
        user?.email?.let { email ->
            val emailUsername = email.substringBefore("@")
            if (emailUsername.isNotBlank()) {
                // Capitalize first letter
                return emailUsername.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                }
            }
        }

        // Final fallback
        return "User"
    }
}
