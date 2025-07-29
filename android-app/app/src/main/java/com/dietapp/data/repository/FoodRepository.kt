package com.dietapp.data.repository

import com.dietapp.data.dao.FoodDao
import com.dietapp.data.dao.FoodLogDao
import com.dietapp.data.entities.Food
import com.dietapp.data.entities.FoodLog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for food and meal operations
 * Implements FR4.1, FR4.2, FR4.3, FR4.4, FR4.5, FR5.1, FR5.2, FR5.4
 */
@Singleton
class FoodRepository @Inject constructor(
    private val foodDao: FoodDao,
    private val foodLogDao: FoodLogDao,
    private val firestore: FirebaseFirestore
) {

    // Food Item Operations (FR5.1)
    fun searchFoods(query: String): Flow<List<Food>> {
        return foodDao.searchFoods(query)
    }

    suspend fun getFoodByBarcode(barcode: String): Food? {
        return foodDao.getFoodByBarcode(barcode)
    }

    suspend fun getFoodById(id: String): Food? {
        return foodDao.getFoodById(id)
    }

    suspend fun insertFood(food: Food) {
        try {
            // Save to Firebase Firestore with proper Timestamp conversion
            val foodData = hashMapOf(
                "id" to food.id,
                "name" to food.name,
                "brand" to food.brand,
                "calories" to food.calories,
                "protein" to food.protein,
                "carbs" to food.carbs,
                "fat" to food.fat,
                "fiber" to food.fiber,
                "sugar" to food.sugar,
                "sodium" to food.sodium,
                "barcode" to food.barcode,
                "servingSize" to food.servingSize,
                "servingUnit" to food.servingUnit,
                "isCustom" to food.isCustom,
                "createdAt" to com.google.firebase.Timestamp(food.createdAt)
            )

            firestore.collection("foods").document(food.id).set(foodData).await()
            println("DEBUG FoodRepository: Saved food to Firebase with ID: ${food.id}")

            // Also save to local database for offline access
            foodDao.insertFood(food)
            println("DEBUG FoodRepository: Saved food to local database")

        } catch (e: Exception) {
            println("DEBUG FoodRepository: Error saving food to Firebase: ${e.message}")
            // Fallback to local database only
            foodDao.insertFood(food)
        }
    }

    suspend fun insertFoods(foods: List<Food>) {
        foodDao.insertFoods(foods)
    }

    fun getAllFoods(limit: Int = 100): Flow<List<Food>> {
        return foodDao.getAllFoods(limit)
    }

    fun getCustomFoods(): Flow<List<Food>> {
        return foodDao.getCustomFoods()
    }

    // Food Log Operations
    suspend fun insertFoodLog(foodLog: FoodLog) {
        try {
            // Save to Firebase Firestore with proper Timestamp conversion
            val logData = hashMapOf(
                "userId" to foodLog.userId,
                "foodId" to foodLog.foodId,
                "quantity" to foodLog.quantity,
                "unit" to foodLog.unit,
                "mealType" to foodLog.mealType,
                "date" to com.google.firebase.Timestamp(foodLog.date),
                "calories" to foodLog.calories,
                "protein" to foodLog.protein,
                "carbs" to foodLog.carbs,
                "fat" to foodLog.fat,
                "createdAt" to com.google.firebase.Timestamp(foodLog.createdAt)
            )

            val documentReference = firestore.collection("foodLogs").add(logData).await()
            println("DEBUG FoodRepository: Saved food log to Firebase with ID: ${documentReference.id}")

            // Also save to local database for offline access
            foodLogDao.insertFoodLog(foodLog)
            println("DEBUG FoodRepository: Saved food log to local database")

        } catch (e: Exception) {
            println("DEBUG FoodRepository: Error saving food log to Firebase: ${e.message}")
            // Fallback to local database only
            foodLogDao.insertFoodLog(foodLog)
        }
    }

    fun getFoodLogsForDate(userId: String, date: Date): Flow<List<FoodLog>> = callbackFlow {
        // Create start and end of day for the date range query
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = com.google.firebase.Timestamp(calendar.time)

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = com.google.firebase.Timestamp(calendar.time)

        val listener = firestore.collection("foodLogs")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("date", startOfDay)
            .whereLessThanOrEqualTo("date", endOfDay)
            .orderBy("date", Query.Direction.ASCENDING)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("DEBUG FoodRepository: Error getting food logs from Firebase: ${error.message}")
                    // Fallback to local database
                    launch {
                        try {
                            foodLogDao.getFoodLogsByDate(userId, date).collect { localLogs ->
                                trySend(localLogs)
                            }
                        } catch (e: Exception) {
                            println("DEBUG FoodRepository: Error getting local food logs: ${e.message}")
                            trySend(emptyList())
                        }
                    }
                    return@addSnapshotListener
                }

                val logs = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        if (data != null) {
                            FoodLog(
                                id = doc.id.hashCode().toLong(),
                                userId = data["userId"] as? String ?: "",
                                foodId = data["foodId"] as? String ?: "",
                                quantity = (data["quantity"] as? Number)?.toDouble() ?: 0.0,
                                unit = data["unit"] as? String ?: "g",
                                mealType = data["mealType"] as? String ?: "",
                                date = (data["date"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                                calories = (data["calories"] as? Number)?.toDouble() ?: 0.0,
                                protein = (data["protein"] as? Number)?.toDouble() ?: 0.0,
                                carbs = (data["carbs"] as? Number)?.toDouble() ?: 0.0,
                                fat = (data["fat"] as? Number)?.toDouble() ?: 0.0,
                                createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()
                            )
                        } else null
                    } catch (e: Exception) {
                        println("DEBUG FoodRepository: Error parsing food log from doc ${doc.id}: ${e.message}")
                        null
                    }
                } ?: emptyList()

                println("DEBUG FoodRepository: Retrieved ${logs.size} food logs from Firestore for date: $date")

                // If Firestore returns no results, also try local database as backup
                if (logs.isEmpty()) {
                    println("DEBUG FoodRepository: No Firestore results, checking local database as backup...")
                    launch {
                        try {
                            foodLogDao.getFoodLogsByDate(userId, date).take(1).collect { localLogs ->
                                println("DEBUG FoodRepository: Local database returned ${localLogs.size} logs")
                                if (localLogs.isNotEmpty()) {
                                    trySend(localLogs)
                                } else {
                                    trySend(logs) // Send empty Firestore results
                                }
                            }
                        } catch (e: Exception) {
                            println("DEBUG FoodRepository: Error getting local food logs: ${e.message}")
                            trySend(logs) // Send empty Firestore results
                        }
                    }
                } else {
                    trySend(logs)
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun deleteFoodLog(foodLog: FoodLog) {
        try {
            // Delete from Firebase
            val querySnapshot = firestore.collection("foodLogs")
                .whereEqualTo("userId", foodLog.userId)
                .whereEqualTo("foodId", foodLog.foodId)
                .whereEqualTo("date", foodLog.date)
                .whereEqualTo("createdAt", foodLog.createdAt)
                .get()
                .await()

            querySnapshot.documents.forEach { document ->
                document.reference.delete().await()
            }
            println("DEBUG FoodRepository: Deleted food log from Firebase")

            // Also delete from local database
            foodLogDao.deleteFoodLog(foodLog)
        } catch (e: Exception) {
            println("DEBUG FoodRepository: Error deleting food log from Firebase: ${e.message}")
            // Fallback to local database only
            foodLogDao.deleteFoodLog(foodLog)
        }
    }

    suspend fun updateFoodLog(foodLog: FoodLog) {
        try {
            // Update in Firebase
            val querySnapshot = firestore.collection("foodLogs")
                .whereEqualTo("userId", foodLog.userId)
                .whereEqualTo("foodId", foodLog.foodId)
                .whereEqualTo("date", foodLog.date)
                .whereEqualTo("createdAt", foodLog.createdAt)
                .get()
                .await()

            if (querySnapshot.documents.isNotEmpty()) {
                val document = querySnapshot.documents.first()
                val updateData = hashMapOf(
                    "quantity" to foodLog.quantity,
                    "unit" to foodLog.unit,
                    "mealType" to foodLog.mealType,
                    "calories" to foodLog.calories,
                    "protein" to foodLog.protein,
                    "carbs" to foodLog.carbs,
                    "fat" to foodLog.fat
                )
                document.reference.update(updateData as Map<String, Any>).await()
                println("DEBUG FoodRepository: Updated food log in Firebase")
            }

            // Also update in local database
            foodLogDao.updateFoodLog(foodLog)
        } catch (e: Exception) {
            println("DEBUG FoodRepository: Error updating food log in Firebase: ${e.message}")
            // Fallback to local database only
            foodLogDao.updateFoodLog(foodLog)
        }
    }

    fun getFoodLogsInRange(userId: String, startDate: Date, endDate: Date): Flow<List<FoodLog>> = callbackFlow {
        val listener = firestore.collection("foodLogs")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("date", com.google.firebase.Timestamp(startDate))
            .whereLessThanOrEqualTo("date", com.google.firebase.Timestamp(endDate))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("FoodRepository: Error getting food logs in range: ${error.message}")
                    // Send empty list instead of closing to prevent app crash
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val logs = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        if (data != null) {
                            val userId = data["userId"] as? String
                            val foodId = data["foodId"] as? String
                            val quantity = data["quantity"] as? Double
                            val unit = data["unit"] as? String
                            val calories = data["calories"] as? Double
                            val protein = data["protein"] as? Double
                            val carbs = data["carbs"] as? Double
                            val fat = data["fat"] as? Double
                            val mealType = data["mealType"] as? String

                            val dateTimestamp = data["date"] as? com.google.firebase.Timestamp
                            val createdAtTimestamp = data["createdAt"] as? com.google.firebase.Timestamp
                            val date = dateTimestamp?.toDate() ?: Date()
                            val createdAt = createdAtTimestamp?.toDate() ?: Date()

                            if (userId != null && foodId != null) {
                                FoodLog(
                                    id = doc.id.hashCode().toLong(),
                                    userId = userId,
                                    foodId = foodId,
                                    quantity = quantity ?: 1.0,
                                    unit = unit ?: "g",
                                    mealType = mealType ?: "snack",
                                    date = date,
                                    calories = calories ?: 0.0,
                                    protein = protein ?: 0.0,
                                    carbs = carbs ?: 0.0,
                                    fat = fat ?: 0.0,
                                    createdAt = createdAt
                                )
                            } else null
                        } else null
                    } catch (e: Exception) {
                        println("FoodRepository: Error parsing food log in range: ${e.message}")
                        null
                    }
                } ?: emptyList()

                // Sort by date in the app instead of using orderBy in Firestore
                val sortedLogs = logs.sortedBy { it.date }
                trySend(sortedLogs)
            }

        awaitClose { listener.remove() }
    }

    suspend fun deleteAllFoodLogs(userId: String) {
        try {
            // Delete from Firebase
            val querySnapshot = firestore.collection("foodLogs")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            querySnapshot.documents.forEach { document ->
                document.reference.delete().await()
            }
            println("DEBUG FoodRepository: Deleted all food logs from Firebase for user $userId")

            // Also delete from local database
            foodLogDao.deleteAllFoodLogs(userId)
        } catch (e: Exception) {
            println("DEBUG FoodRepository: Error deleting all food logs from Firebase: ${e.message}")
            // Fallback to local database only
            foodLogDao.deleteAllFoodLogs(userId)
        }
    }
}
