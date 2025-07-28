package com.dietapp.data.repository

import com.dietapp.data.dao.WeightDao
import com.dietapp.data.entities.WeightEntry
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
class WeightRepository @Inject constructor(
    private val weightDao: WeightDao,
    private val firestore: FirebaseFirestore
) {
    fun getAllWeightEntries(userId: String): Flow<List<WeightEntry>> = callbackFlow {
        val listener = firestore.collection("weightEntries")
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("DEBUG WeightRepository: Error getting weight entries from Firebase: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                val entries = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(WeightEntry::class.java)?.copy(id = doc.id.hashCode().toLong())
                    } catch (e: Exception) {
                        println("DEBUG WeightRepository: Error parsing weight entry: ${e.message}")
                        null
                    }
                } ?: emptyList()

                trySend(entries)
            }

        awaitClose { listener.remove() }
    }

    fun getLatestWeightEntry(userId: String): Flow<WeightEntry?> = callbackFlow {
        val listener = firestore.collection("weightEntries")
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("DEBUG WeightRepository: Error getting latest weight entry from Firebase: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                val entry = snapshot?.documents?.firstOrNull()?.let { doc ->
                    try {
                        doc.toObject(WeightEntry::class.java)?.copy(id = doc.id.hashCode().toLong())
                    } catch (e: Exception) {
                        println("DEBUG WeightRepository: Error parsing latest weight entry: ${e.message}")
                        null
                    }
                }

                trySend(entry)
            }

        awaitClose { listener.remove() }
    }

    fun getWeightEntriesInRange(userId: String, startDate: Date, endDate: Date): Flow<List<WeightEntry>> =
        weightDao.getWeightEntriesInRange(userId, startDate, endDate)

    suspend fun insertWeightEntry(weightEntry: WeightEntry): Long {
        try {
            // Save to Firebase Firestore
            val entryData = hashMapOf(
                "userId" to weightEntry.userId,
                "weight" to weightEntry.weight,
                "bodyFatPercentage" to weightEntry.bodyFatPercentage,
                "muscleMass" to weightEntry.muscleMass,
                "date" to weightEntry.date,
                "createdAt" to weightEntry.createdAt
            )

            val documentReference = firestore.collection("weightEntries").add(entryData).await()
            println("DEBUG WeightRepository: Saved weight entry to Firebase with ID: ${documentReference.id}")

            // Also save to local database for offline access
            val localId = weightDao.insertWeightEntry(weightEntry)
            println("DEBUG WeightRepository: Saved weight entry to local database with ID: $localId")

            return localId

        } catch (e: Exception) {
            println("DEBUG WeightRepository: Error saving weight entry to Firebase: ${e.message}")
            // Fallback to local database only
            return weightDao.insertWeightEntry(weightEntry)
        }
    }

    suspend fun updateWeightEntry(weightEntry: WeightEntry) =
        weightDao.updateWeightEntry(weightEntry)

    suspend fun deleteWeightEntry(weightEntry: WeightEntry) =
        weightDao.deleteWeightEntry(weightEntry)

    suspend fun getAverageWeightSince(userId: String, startDate: Date): Double? =
        weightDao.getAverageWeightSince(userId, startDate)

    /**
     * Calculate weight progress for the given period
     */
    suspend fun getWeightProgress(userId: String, days: Int): Pair<Double?, Double?> {
        // This method would calculate weight progress over a period
        // For now, return null to indicate this needs implementation
        // In a real implementation, you would:
        // 1. Calculate date range based on 'days' parameter
        // 2. Query weight entries for the user within that range
        // 3. Calculate the difference between first and last weight
        // 4. Calculate the average rate of change
        return Pair(null, null)
    }

    /**
     * Calculate BMI from weight and height
     */
    fun calculateBMI(weightKg: Double, heightCm: Double): Double {
        val heightM = heightCm / 100.0
        return weightKg / (heightM * heightM)
    }

    /**
     * Get BMI category
     */
    fun getBMICategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal weight"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
    }

    /**
     * Calculate ideal weight range using BMI 18.5-24.9
     */
    fun getIdealWeightRange(heightCm: Double): Pair<Double, Double> {
        val heightM = heightCm / 100.0
        val minWeight = 18.5 * heightM * heightM
        val maxWeight = 24.9 * heightM * heightM
        return Pair(minWeight, maxWeight)
    }
}
