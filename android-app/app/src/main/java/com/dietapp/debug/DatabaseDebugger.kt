package com.dietapp.debug

import com.dietapp.data.repository.FoodRepository
import com.dietapp.data.repository.UserRepository
import com.dietapp.data.repository.WeightRepository
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Debug utility for querying and inspecting Room database content
 * Use this to check what data is stored locally in your app
 */
@Singleton
class DatabaseDebugger @Inject constructor(
    private val foodRepository: FoodRepository,
    private val userRepository: UserRepository,
    private val weightRepository: WeightRepository
) {

    suspend fun printDatabaseSummary(userId: String) {
        println("=== ROOM DATABASE SUMMARY ===")

        try {
            // Check user profile
            val userProfile = userRepository.getUserProfile(userId)
            println("User Profile: ${if (userProfile != null) "EXISTS" else "NOT FOUND"}")
            userProfile?.let {
                println("  - Name: ${it.name}")
                println("  - Email: ${it.email}")
                println("  - Current Weight: ${it.currentWeight}")
                println("  - Daily Calorie Goal: ${it.dailyCalorieGoal}")
            }

            // Check foods in database
            val allFoods = foodRepository.getAllFoods().first()
            println("\nFoods in database: ${allFoods.size}")
            allFoods.take(5).forEach { food ->
                println("  - ${food.name} (${food.calories} cal)")
            }
            if (allFoods.size > 5) {
                println("  ... and ${allFoods.size - 5} more")
            }

            // Check food logs
            val thirtyDaysAgo = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -30)
            }.time
            val today = Date()

            val recentLogs = foodRepository.getFoodLogsInRange(userId, thirtyDaysAgo, today).first()
            println("\nFood logs (last 30 days): ${recentLogs.size}")
            recentLogs.take(5).forEach { log ->
                println("  - ${log.foodId} on ${log.date} (${log.calories} cal)")
            }
            if (recentLogs.size > 5) {
                println("  ... and ${recentLogs.size - 5} more")
            }

            // Check weight entries
            val weightEntries = weightRepository.getWeightEntriesInRange(userId, thirtyDaysAgo, today).first()
            println("\nWeight entries (last 30 days): ${weightEntries.size}")
            weightEntries.take(5).forEach { entry ->
                println("  - ${entry.weight} kg on ${entry.date}")
            }
            if (weightEntries.size > 5) {
                println("  ... and ${weightEntries.size - 5} more")
            }

            // Today's summary
            val todayLogs = foodRepository.getFoodLogsForDate(userId, today).first()
            val todayCalories = todayLogs.sumOf { it.calories }
            val todayProtein = todayLogs.sumOf { it.protein }
            val todayCarbs = todayLogs.sumOf { it.carbs }
            val todayFat = todayLogs.sumOf { it.fat }

            println("\nToday's nutrition:")
            println("  - Calories: $todayCalories")
            println("  - Protein: ${todayProtein}g")
            println("  - Carbs: ${todayCarbs}g")
            println("  - Fat: ${todayFat}g")
            println("  - Logged meals: ${todayLogs.size}")

        } catch (e: Exception) {
            println("Error querying database: ${e.message}")
            e.printStackTrace()
        }

        println("=== END DATABASE SUMMARY ===")
    }

    suspend fun printTodaysFoodLogs(userId: String) {
        println("=== TODAY'S FOOD LOGS ===")
        try {
            val today = Date()
            val todayLogs = foodRepository.getFoodLogsForDate(userId, today).first()

            if (todayLogs.isEmpty()) {
                println("No food logs for today")
            } else {
                println("Found ${todayLogs.size} food logs for today:")
                todayLogs.forEach { log ->
                    println("  - Food ID: ${log.foodId}")
                    println("    Quantity: ${log.quantity}")
                    println("    Calories: ${log.calories}")
                    println("    Meal Type: ${log.mealType}")
                    println("    Time: ${log.date}")
                    println("    ---")
                }
            }
        } catch (e: Exception) {
            println("Error querying today's logs: ${e.message}")
        }
        println("=== END TODAY'S LOGS ===")
    }

    suspend fun printAllFoods() {
        println("=== ALL FOODS IN DATABASE ===")
        try {
            val allFoods = foodRepository.getAllFoods().first()

            if (allFoods.isEmpty()) {
                println("No foods in database")
            } else {
                println("Found ${allFoods.size} foods:")
                allFoods.forEach { food ->
                    println("  - ID: ${food.id}")
                    println("    Name: ${food.name}")
                    println("    Brand: ${food.brand}")
                    println("    Calories: ${food.calories}")
                    println("    Custom: ${food.isCustom}")
                    println("    ---")
                }
            }
        } catch (e: Exception) {
            println("Error querying foods: ${e.message}")
        }
        println("=== END ALL FOODS ===")
    }

    suspend fun searchFoodInDatabase(query: String) {
        println("=== SEARCHING FOODS: '$query' ===")
        try {
            val searchResults = foodRepository.searchFoods(query).first()

            if (searchResults.isEmpty()) {
                println("No foods found matching '$query'")
            } else {
                println("Found ${searchResults.size} foods matching '$query':")
                searchResults.forEach { food ->
                    println("  - ${food.name} (${food.calories} cal)")
                }
            }
        } catch (e: Exception) {
            println("Error searching foods: ${e.message}")
        }
        println("=== END SEARCH ===")
    }

    // === DELETE FUNCTIONS ===

    suspend fun deleteAllWeightEntries(userId: String) {
        println("=== DELETING ALL WEIGHT ENTRIES FOR USER: $userId ===")
        try {
            // First, let's see what weight entries exist for this user
            val userEntries = weightRepository.getAllWeightEntries(userId).first()
            println("DEBUG: Found ${userEntries.size} weight entries for user $userId:")
            userEntries.take(3).forEach { entry ->
                println("  - ${entry.weight} kg on ${entry.date} (userId: ${entry.userId})")
            }

            // Also check if there are entries for other users
            val allEntries = weightRepository.getAllWeightEntriesDebug()
            println("DEBUG: Total weight entries in database: ${allEntries.size}")
            val uniqueUserIds = allEntries.map { it.userId }.distinct()
            println("DEBUG: Unique user IDs in database: $uniqueUserIds")

            val deletedCount = weightRepository.deleteAllWeightEntries(userId)
            println("Successfully deleted $deletedCount weight entries for user $userId")
        } catch (e: Exception) {
            println("Error deleting weight entries: ${e.message}")
            e.printStackTrace()
        }
        println("=== END DELETE WEIGHT ENTRIES ===")
    }

    suspend fun deleteWeightEntriesForSpecificUser(targetUserId: String) {
        println("=== DELETING WEIGHT ENTRIES FOR SPECIFIC USER: $targetUserId ===")
        try {
            // Show what we're about to delete
            val allEntries = weightRepository.getAllWeightEntriesDebug()
            println("DEBUG: Total weight entries in database: ${allEntries.size}")

            val uniqueUserIds = allEntries.map { it.userId }.distinct()
            println("DEBUG: Unique user IDs in database: $uniqueUserIds")

            val targetEntries = allEntries.filter { it.userId == targetUserId }
            println("DEBUG: Found ${targetEntries.size} weight entries for user $targetUserId:")
            targetEntries.take(5).forEach { entry ->
                println("  - ${entry.weight} kg on ${entry.date}")
            }
            if (targetEntries.size > 5) {
                println("  ... and ${targetEntries.size - 5} more entries")
            }

            val deletedCount = weightRepository.deleteAllWeightEntries(targetUserId)
            println("Successfully deleted $deletedCount weight entries for user $targetUserId")
        } catch (e: Exception) {
            println("Error deleting weight entries for user $targetUserId: ${e.message}")
            e.printStackTrace()
        }
        println("=== END DELETE WEIGHT ENTRIES FOR SPECIFIC USER ===")
    }

    suspend fun showAllUserIdsInDatabase() {
        println("=== DATABASE USER ID ANALYSIS ===")
        try {
            val allEntries = weightRepository.getAllWeightEntriesDebug()
            println("Total weight entries in database: ${allEntries.size}")

            val userIdCounts = allEntries.groupBy { it.userId }.mapValues { it.value.size }
            println("User ID breakdown:")
            userIdCounts.forEach { (userId, count) ->
                println("  - User $userId: $count entries")
            }

            if (allEntries.isNotEmpty()) {
                println("Sample entries:")
                allEntries.take(3).forEach { entry ->
                    println("  - ${entry.weight} kg on ${entry.date} (userId: ${entry.userId})")
                }
            }
        } catch (e: Exception) {
            println("Error analyzing database: ${e.message}")
            e.printStackTrace()
        }
        println("=== END DATABASE USER ID ANALYSIS ===")
    }

    suspend fun deleteWeightEntriesForSpecificUserWithFirestore(targetUserId: String) {
        println("=== DELETING WEIGHT ENTRIES (ROOM + FIRESTORE) FOR USER: $targetUserId ===")
        try {
            // Show what we're about to delete
            val allEntries = weightRepository.getAllWeightEntriesDebug()
            println("DEBUG: Total weight entries in Room database: ${allEntries.size}")

            val uniqueUserIds = allEntries.map { it.userId }.distinct()
            println("DEBUG: Unique user IDs in Room database: $uniqueUserIds")

            val targetEntries = allEntries.filter { it.userId == targetUserId }
            println("DEBUG: Found ${targetEntries.size} weight entries in Room for user $targetUserId:")
            targetEntries.take(5).forEach { entry ->
                println("  - ${entry.weight} kg on ${entry.date}")
            }
            if (targetEntries.size > 5) {
                println("  ... and ${targetEntries.size - 5} more entries")
            }

            val deletedCount = weightRepository.deleteAllWeightEntriesWithFirestore(targetUserId)
            println("Successfully deleted $deletedCount weight entries (Room + Firestore) for user $targetUserId")
        } catch (e: Exception) {
            println("Error deleting weight entries from Room + Firestore for user $targetUserId: ${e.message}")
            e.printStackTrace()
        }
        println("=== END DELETE WEIGHT ENTRIES (ROOM + FIRESTORE) FOR SPECIFIC USER ===")
    }

    suspend fun deleteAllWeightEntriesGlobalWithFirestore() {
        println("=== DELETING ALL WEIGHT ENTRIES (ROOM + FIRESTORE) ===")
        try {
            val deletedCount = weightRepository.deleteAllWeightEntriesGlobalWithFirestore()
            println("Successfully deleted $deletedCount weight entries from Room + Firestore (all users)")
        } catch (e: Exception) {
            println("Error deleting all weight entries from Room + Firestore: ${e.message}")
            e.printStackTrace()
        }
        println("=== END DELETE ALL WEIGHT ENTRIES (ROOM + FIRESTORE) ===")
    }

    suspend fun deleteAllWeightEntriesGlobal() {
        println("=== DELETING ALL WEIGHT ENTRIES (ALL USERS) ===")
        try {
            val deletedCount = weightRepository.deleteAllWeightEntriesGlobal()
            println("Successfully deleted $deletedCount weight entries from all users")
        } catch (e: Exception) {
            println("Error deleting all weight entries: ${e.message}")
            e.printStackTrace()
        }
        println("=== END DELETE ALL WEIGHT ENTRIES ===")
    }

    suspend fun deleteAllFoodLogs(userId: String) {
        println("=== DELETING ALL FOOD LOGS FOR USER: $userId ===")
        try {
            val deletedCount = foodRepository.deleteAllFoodLogs(userId)
            println("Successfully deleted $deletedCount food logs for user $userId")
        } catch (e: Exception) {
            println("Error deleting food logs: ${e.message}")
            e.printStackTrace()
        }
        println("=== END DELETE FOOD LOGS ===")
    }

    suspend fun clearAllUserData(userId: String) {
        println("=== CLEARING ALL DATA FOR USER: $userId ===")
        try {
            val weightDeleted = weightRepository.deleteAllWeightEntries(userId)
            val logsDeleted = foodRepository.deleteAllFoodLogs(userId)

            println("Deleted $weightDeleted weight entries")
            println("Deleted $logsDeleted food logs")
            println("User data cleared successfully")
        } catch (e: Exception) {
            println("Error clearing user data: ${e.message}")
            e.printStackTrace()
        }
        println("=== END CLEAR USER DATA ===")
    }
}
