package com.dietapp

import android.app.Application
import com.google.firebase.FirebaseApp
import com.dietapp.auth.AuthRepository
import com.dietapp.data.seeddata.SeedDataManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class DietApp : Application() {

    @Inject
    lateinit var seedDataManager: SeedDataManager

    @Inject
    lateinit var authRepository: AuthRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Seed the database with sample data
        seedDataManager.seedDatabase()

        // Observe authentication state and seed user data when authenticated
        observeAuthState()
    }

    private fun observeAuthState() {
        applicationScope.launch {
            authRepository.authState.collect { authState ->
                if (authState is com.dietapp.auth.AuthState.Authenticated) {
                    // Seed sample user data for newly authenticated users
                    seedDataManager.seedSampleUserData(authState.userId)
                }
            }
        }
    }
}
