package com.dietapp.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dietapp.auth.AuthState
import com.dietapp.ui.screens.*
import com.dietapp.ui.screens.food.FoodSearchScreen
import com.dietapp.ui.scanner.BarcodeScannerScreen
import com.dietapp.ui.charts.ChartsScreen
import com.dietapp.ui.profile.ProfileScreen
import com.dietapp.ui.simple.*
import com.dietapp.ui.viewmodels.AuthViewModel

@Composable
fun DietAppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    // Determine the start destination based on auth state
    val startDestination = when (authState) {
        is AuthState.Authenticated -> "dashboard"
        is AuthState.Unauthenticated -> "login"
        else -> "login" // Loading or Error states default to login
    }

    // Navigate based on auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                navController.navigate("dashboard") {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }
            }
            is AuthState.Unauthenticated -> {
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }
            }
            else -> { /* Handle loading/error states if needed */ }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Authentication
        composable("login") {
            LoginScreen(
                onNavigateToDashboard = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // Main app screens
        composable("dashboard") {
            DashboardScreen(
                onNavigateToFoodLogging = {
                    navController.navigate("food_logging")
                },
                onNavigateToBarcode = {
                    navController.navigate("barcode_scanner")
                },
                onNavigateToCharts = {
                    navController.navigate("charts")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onNavigateToAdvancedFeatures = {
                    navController.navigate("advanced_features")
                }
            )
        }

        // Food Logging
        composable("food_logging") { backStackEntry ->
            FoodLoggingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToFoodSearch = { mealType ->
                    navController.navigate("food_search/$mealType")
                },
                onNavigateToBarCodeScanner = {
                    navController.navigate("barcode_scanner")
                },
                savedStateHandle = backStackEntry.savedStateHandle
            )
        }

        // Food Search
        composable(
            route = "food_search/{mealType}",
            arguments = listOf(navArgument("mealType") { type = NavType.StringType })
        ) { backStackEntry ->
            val mealType = backStackEntry.arguments?.getString("mealType") ?: "Breakfast"
            FoodSearchScreen(
                mealType = mealType,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onFoodSelected = { food: com.dietapp.data.entities.Food, selectedMealType: String ->
                    // Pass food data as individual values since Food entity is not Parcelable
                    navController.previousBackStackEntry?.savedStateHandle?.set("food_id", food.id)
                    navController.previousBackStackEntry?.savedStateHandle?.set("food_name", food.name)
                    navController.previousBackStackEntry?.savedStateHandle?.set("food_brand", food.brand)
                    navController.previousBackStackEntry?.savedStateHandle?.set("food_calories", food.calories)
                    navController.previousBackStackEntry?.savedStateHandle?.set("food_protein", food.protein)
                    navController.previousBackStackEntry?.savedStateHandle?.set("food_carbs", food.carbs)
                    navController.previousBackStackEntry?.savedStateHandle?.set("food_fat", food.fat)
                    navController.previousBackStackEntry?.savedStateHandle?.set("food_fiber", food.fiber)
                    navController.previousBackStackEntry?.savedStateHandle?.set("food_sugar", food.sugar)
                    navController.previousBackStackEntry?.savedStateHandle?.set("food_sodium", food.sodium)
                    navController.previousBackStackEntry?.savedStateHandle?.set("food_barcode", food.barcode)
                    navController.previousBackStackEntry?.savedStateHandle?.set("food_serving_size", food.servingSize)
                    navController.previousBackStackEntry?.savedStateHandle?.set("food_serving_unit", food.servingUnit)
                    navController.previousBackStackEntry?.savedStateHandle?.set("food_is_custom", food.isCustom)
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_meal_type", selectedMealType)
                    navController.popBackStack()
                }
            )
        }

        // Barcode Scanner
        composable("barcode_scanner") {
            SimpleBarcodeScannerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartScanning = {
                    navController.navigate("camera_scanner")
                }
            )
        }

        // Camera Scanner
        composable("camera_scanner") {
            BarcodeScannerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Charts and Analytics
        composable("charts") {
            SimpleChartsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Advanced Features Hub
        composable("advanced_features") {
            AdvancedFeaturesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToGoalSetup = {
                    navController.navigate("goal_setup")
                },
                onNavigateToWaterTracking = {
                    navController.navigate("water_tracking")
                },
                onNavigateToProgressTracking = {
                    navController.navigate("progress_tracking")
                }
            )
        }

        // Goal Setup
        composable("goal_setup") {
            GoalSetupScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onGoalCreated = {
                    navController.popBackStack()
                }
            )
        }

        // Water Tracking
        composable("water_tracking") {
            WaterTrackingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Progress Tracking (Weight tracking)
        composable("progress_tracking") {
            SimpleProgressTrackingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Profile Screen
        composable("profile") {
            SimpleProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }

    // Listen for auth state changes and navigate accordingly
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Unauthenticated -> {
                if (navController.currentDestination?.route != "login") {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is AuthState.Authenticated -> {
                if (navController.currentDestination?.route == "login") {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
            else -> { /* Handle loading or error states if needed */ }
        }
    }
}
