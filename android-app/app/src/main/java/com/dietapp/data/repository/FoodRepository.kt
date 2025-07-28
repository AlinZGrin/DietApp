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
            // Save to Firebase Firestore
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
                "createdAt" to food.createdAt
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
            // Save to Firebase Firestore
            val logData = hashMapOf(
                "userId" to foodLog.userId,
                "foodId" to foodLog.foodId,
                "quantity" to foodLog.quantity,
                "unit" to foodLog.unit,
                "mealType" to foodLog.mealType,
                "date" to foodLog.date,
                "calories" to foodLog.calories,
                "protein" to foodLog.protein,
                "carbs" to foodLog.carbs,
                "fat" to foodLog.fat,
                "createdAt" to foodLog.createdAt
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
        val listener = firestore.collection("foodLogs")
            .whereEqualTo("userId", userId)
            .whereEqualTo("date", date)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("DEBUG FoodRepository: Error getting food logs from Firebase: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                val logs = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(FoodLog::class.java)?.copy(id = doc.id.hashCode().toLong())
                    } catch (e: Exception) {
                        println("DEBUG FoodRepository: Error parsing food log: ${e.message}")
                        null
                    }
                } ?: emptyList()

                trySend(logs)
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
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("DEBUG FoodRepository: Error getting food logs in range from Firebase: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                val logs = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(FoodLog::class.java)?.copy(id = doc.id.hashCode().toLong())
                    } catch (e: Exception) {
                        println("DEBUG FoodRepository: Error parsing food log: ${e.message}")
                        null
                    }
                } ?: emptyList()

                trySend(logs)
            }

        awaitClose { listener.remove() }
    }
}
