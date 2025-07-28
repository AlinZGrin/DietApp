package com.dietapp.data.repository

import com.dietapp.data.dao.GoalDao
import com.dietapp.data.entities.Goal
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val firestore: FirebaseFirestore
) {
    // Simple goal retrieval that bypasses complex object construction
    fun getSimpleGoal(userId: String): Flow<Goal?> = callbackFlow {
        val listener = firestore.collection("goals")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Keep error logging for troubleshooting
                    println("GoalRepository: Error getting goal: ${error.message}")
                    trySend(null)
                    return@addSnapshotListener
                }

                val activeGoal = snapshot?.documents?.firstOrNull()?.let { doc ->
                    try {
                        val targetWeight = doc.getDouble("targetWeight")
                        val userId = doc.getString("userId")

                        if (targetWeight != null && userId != null) {
                            Goal(
                                id = doc.id.hashCode().toLong(),
                                userId = userId,
                                goalType = doc.getString("goalType") ?: "weight_loss",
                                targetWeight = targetWeight,
                                targetCalories = doc.getDouble("targetCalories") ?: 2000.0,
                                targetProtein = doc.getDouble("targetProtein") ?: 150.0,
                                targetCarbs = doc.getDouble("targetCarbs") ?: 200.0,
                                targetFat = doc.getDouble("targetFat") ?: 67.0,
                                targetDate = null,
                                weeklyWeightLossGoal = doc.getDouble("weeklyWeightLossGoal") ?: 0.0,
                                activityLevel = doc.getString("activityLevel") ?: "moderate",
                                currentHeight = doc.getDouble("currentHeight"),
                                useImperialUnits = doc.getBoolean("useImperialUnits") ?: false,
                                isActive = true, // We already filtered for this
                                createdAt = Date(),
                                updatedAt = Date()
                            )
                        } else null
                    } catch (e: Exception) {
                        println("GoalRepository: Error constructing goal: ${e.message}")
                        null
                    }
                }

                trySend(activeGoal)
            }

        awaitClose {
            listener.remove()
        }
    }    // Get all goals from Firebase with local fallback
    fun getAllGoals(userId: String): Flow<List<Goal>> = callbackFlow {
        val listener = firestore.collection("goals")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("DEBUG GoalRepository: Error getting goals from Firebase: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                val goals = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Goal::class.java)?.copy(id = doc.id.hashCode().toLong())
                    } catch (e: Exception) {
                        println("DEBUG GoalRepository: Error parsing goal document: ${e.message}")
                        null
                    }
                } ?: emptyList()

                trySend(goals)
            }

        awaitClose { listener.remove() }
    }

    suspend fun createNewGoal(goal: Goal): Long {
        try {
            // First, deactivate all previous goals in Firebase
            deactivateAllGoals(goal.userId)

            // Create goal data for Firebase (without auto-generated id)
            val goalData = hashMapOf(
                "userId" to goal.userId,
                "goalType" to goal.goalType,
                "targetWeight" to goal.targetWeight,
                "targetCalories" to goal.targetCalories,
                "targetProtein" to goal.targetProtein,
                "targetCarbs" to goal.targetCarbs,
                "targetFat" to goal.targetFat,
                "targetDate" to goal.targetDate,
                "weeklyWeightLossGoal" to goal.weeklyWeightLossGoal,
                "activityLevel" to goal.activityLevel,
                "currentHeight" to goal.currentHeight,
                "useImperialUnits" to goal.useImperialUnits,
                "isActive" to goal.isActive,
                "createdAt" to goal.createdAt,
                "updatedAt" to goal.updatedAt
            )

            // Save to Firebase Firestore
            val documentReference = firestore.collection("goals").add(goalData).await()

            // Also save to local database for offline access
            goalDao.deactivateAllGoals(goal.userId)
            val localId = goalDao.insertGoal(goal)

            return localId

        } catch (e: Exception) {
            println("GoalRepository: Error saving goal to Firebase: ${e.message}")
            // Fallback to local database only
            goalDao.deactivateAllGoals(goal.userId)
            val result = goalDao.insertGoal(goal)
            return result
        }
    }

    suspend fun updateGoal(goal: Goal) {
        try {
            // Update in Firebase
            val goalData = mapOf(
                "userId" to goal.userId,
                "goalType" to goal.goalType,
                "targetWeight" to goal.targetWeight,
                "targetCalories" to goal.targetCalories,
                "targetProtein" to goal.targetProtein,
                "targetCarbs" to goal.targetCarbs,
                "targetFat" to goal.targetFat,
                "targetDate" to goal.targetDate,
                "weeklyWeightLossGoal" to goal.weeklyWeightLossGoal,
                "activityLevel" to goal.activityLevel,
                "currentHeight" to goal.currentHeight,
                "useImperialUnits" to goal.useImperialUnits,
                "isActive" to goal.isActive,
                "updatedAt" to Date()
            )

            // Find and update the goal in Firebase
            val querySnapshot = firestore.collection("goals")
                .whereEqualTo("userId", goal.userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents.first()
                document.reference.update(goalData).await()
                println("DEBUG GoalRepository: Updated goal in Firebase")
            }

            // Also update in local database
            goalDao.updateGoal(goal)
        } catch (e: Exception) {
            println("DEBUG GoalRepository: Error updating goal in Firebase: ${e.message}")
            // Fallback to local database only
            goalDao.updateGoal(goal)
        }
    }

    suspend fun deleteGoal(goal: Goal) {
        try {
            // Delete from Firebase
            val querySnapshot = firestore.collection("goals")
                .whereEqualTo("userId", goal.userId)
                .whereEqualTo("goalType", goal.goalType)
                .whereEqualTo("createdAt", goal.createdAt)
                .get()
                .await()

            querySnapshot.documents.forEach { document ->
                document.reference.delete().await()
            }
            println("DEBUG GoalRepository: Deleted goal from Firebase")

            // Also delete from local database
            goalDao.deleteGoal(goal)
        } catch (e: Exception) {
            println("DEBUG GoalRepository: Error deleting goal from Firebase: ${e.message}")
            // Fallback to local database only
            goalDao.deleteGoal(goal)
        }
    }

    suspend fun deactivateAllGoals(userId: String) {
        try {
            // Deactivate all goals in Firebase
            val querySnapshot = firestore.collection("goals")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            querySnapshot.documents.forEach { document ->
                document.reference.update("isActive", false).await()
            }
            println("DEBUG GoalRepository: Deactivated all goals in Firebase for user $userId")

            // Also deactivate in local database
            goalDao.deactivateAllGoals(userId)
        } catch (e: Exception) {
            println("DEBUG GoalRepository: Error deactivating goals in Firebase: ${e.message}")
            // Fallback to local database only
            goalDao.deactivateAllGoals(userId)
        }
    }

    /**
     * Calculate BMR (Basal Metabolic Rate) using Mifflin-St Jeor Equation
     */
    fun calculateBMR(weight: Double, height: Double, age: Int, isMale: Boolean): Double {
        return if (isMale) {
            10 * weight + 6.25 * height - 5 * age + 5
        } else {
            10 * weight + 6.25 * height - 5 * age - 161
        }
    }

    /**
     * Calculate TDEE (Total Daily Energy Expenditure) based on activity level
     */
    fun calculateTDEE(bmr: Double, activityLevel: String): Double {
        val activityMultiplier = when (activityLevel) {
            "sedentary" -> 1.2
            "light" -> 1.375
            "moderate" -> 1.55
            "active" -> 1.725
            "very_active" -> 1.9
            else -> 1.55 // default to moderate
        }
        return bmr * activityMultiplier
    }

    /**
     * Calculate target calories based on goal type and weekly weight change goal
     */
    fun calculateTargetCalories(
        tdee: Double,
        goalType: String,
        weeklyWeightChangeGoal: Double
    ): Double {
        // 1 kg of fat ≈ 7700 calories
        val dailyCalorieAdjustment = (weeklyWeightChangeGoal * 7700) / 7

        return when (goalType) {
            "weight_loss" -> tdee - Math.abs(dailyCalorieAdjustment)
            "weight_gain" -> tdee + Math.abs(dailyCalorieAdjustment)
            "muscle_gain" -> tdee + 300 // moderate surplus for muscle gain
            else -> tdee // maintenance
        }
    }

    /**
     * Calculate macro targets based on goal type and total calories
     */
    fun calculateMacroTargets(totalCalories: Double, goalType: String): Triple<Double, Double, Double> {
        val (proteinRatio, carbRatio, fatRatio) = when (goalType) {
            "weight_loss" -> Triple(0.35, 0.35, 0.30) // Higher protein for muscle preservation
            "muscle_gain" -> Triple(0.30, 0.45, 0.25) // Higher carbs for performance
            "weight_gain" -> Triple(0.25, 0.50, 0.25) // Balanced for healthy weight gain
            else -> Triple(0.25, 0.45, 0.30) // Maintenance
        }

        val proteinCalories = totalCalories * proteinRatio
        val carbCalories = totalCalories * carbRatio
        val fatCalories = totalCalories * fatRatio

        return Triple(
            proteinCalories / 4, // 4 calories per gram of protein
            carbCalories / 4,    // 4 calories per gram of carbs
            fatCalories / 9      // 9 calories per gram of fat
        )
    }
}
