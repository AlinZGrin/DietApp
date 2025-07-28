package com.dietapp.data.repository

import com.dietapp.data.dao.WeightDao
import com.dietapp.data.entities.WeightEntry
import com.google.firebase.auth.FirebaseAuth
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
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("WeightRepository: Error getting weight entries: ${error.message}")
                    // Fallback to local database
                    // TODO: Implement local fallback
                    close(error)
                    return@addSnapshotListener
                }

                val entries = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        // Manual parsing to handle Firestore Timestamp conversion
                        val data = doc.data
                        if (data != null) {
                            val userId = data["userId"] as? String
                            val weight = data["weight"] as? Double
                            val bodyFatPercentage = data["bodyFatPercentage"] as? Double
                            val muscleMass = data["muscleMass"] as? Double
                            val notes = data["notes"] as? String

                            // Handle Firestore Timestamp to Date conversion
                            val dateTimestamp = data["date"] as? com.google.firebase.Timestamp
                            val createdAtTimestamp = data["createdAt"] as? com.google.firebase.Timestamp

                            val date = dateTimestamp?.toDate() ?: Date()
                            val createdAt = createdAtTimestamp?.toDate() ?: Date()

                            if (userId != null && weight != null) {
                                val entry = WeightEntry(
                                    id = doc.id.hashCode().toLong(),
                                    userId = userId,
                                    weight = weight,
                                    bodyFatPercentage = bodyFatPercentage,
                                    muscleMass = muscleMass,
                                    date = date,
                                    notes = notes,
                                    createdAt = createdAt
                                )
                                println("DEBUG WeightRepository: Parsed entry: ${entry.weight}kg on ${entry.date} for user ${entry.userId}")
                                entry
                            } else {
                                println("DEBUG WeightRepository: Missing required fields - userId: $userId, weight: $weight")
                                null
                            }
                        } else {
                            println("DEBUG WeightRepository: Document data is null")
                            null
                        }
                    } catch (e: Exception) {
                        println("DEBUG WeightRepository: Error parsing weight entry: ${e.message}")
                        e.printStackTrace()
                        null
                    }
                } ?: emptyList()

                // Sort by date in descending order (most recent first) in the app
                val sortedEntries = entries.sortedByDescending { it.date }
                println("DEBUG WeightRepository: Returning ${sortedEntries.size} sorted entries")
                trySend(sortedEntries)
            }

        awaitClose {
            println("DEBUG WeightRepository: Closing getAllWeightEntries listener")
            listener.remove()
        }
    }

    fun getLatestWeightEntry(userId: String): Flow<WeightEntry?> = callbackFlow {
        val listener = firestore.collection("weightEntries")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("DEBUG WeightRepository: Error getting latest weight entry from Firebase: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                val entries = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(WeightEntry::class.java)?.copy(id = doc.id.hashCode().toLong())
                    } catch (e: Exception) {
                        println("DEBUG WeightRepository: Error parsing latest weight entry: ${e.message}")
                        null
                    }
                } ?: emptyList()

                // Get the most recent entry by sorting in the app
                val latestEntry = entries.maxByOrNull { it.date }
                trySend(latestEntry)
            }

        awaitClose { listener.remove() }
    }

    fun getWeightEntriesInRange(userId: String, startDate: Date, endDate: Date): Flow<List<WeightEntry>> =
        weightDao.getWeightEntriesInRange(userId, startDate, endDate)

    suspend fun insertWeightEntry(weightEntry: WeightEntry): Long {
        try {
            // First, save to local database for immediate availability
            val localId = weightDao.insertWeightEntry(weightEntry)
            println("DEBUG WeightRepository: Saved weight entry to local database with ID: $localId")

            // Check authentication status
            val currentUser = FirebaseAuth.getInstance().currentUser
            println("DEBUG WeightRepository: Current Firebase user: ${currentUser?.uid}")
            println("DEBUG WeightRepository: Is user authenticated: ${currentUser != null}")
            println("DEBUG WeightRepository: User email: ${currentUser?.email}")

            // Save to Firebase Firestore with better date handling
            val entryData = hashMapOf(
                "userId" to weightEntry.userId,
                "weight" to weightEntry.weight,
                "bodyFatPercentage" to weightEntry.bodyFatPercentage,
                "muscleMass" to weightEntry.muscleMass,
                "date" to com.google.firebase.Timestamp(weightEntry.date), // Convert Date to Firestore Timestamp
                "createdAt" to com.google.firebase.Timestamp(weightEntry.createdAt),
                "notes" to weightEntry.notes
            )

            println("DEBUG WeightRepository: Attempting to save weight entry for userId: ${weightEntry.userId}")
            println("DEBUG WeightRepository: Entry data: $entryData")
            val documentReference = firestore.collection("weightEntries").add(entryData).await()
            println("DEBUG WeightRepository: Saved weight entry to Firebase with ID: ${documentReference.id}")

            return localId

        } catch (e: Exception) {
            println("DEBUG WeightRepository: Error saving weight entry to Firebase: ${e.message}")
            println("DEBUG WeightRepository: Error type: ${e::class.java.simpleName}")
            if (e.message?.contains("PERMISSION_DENIED") == true) {
                println("DEBUG WeightRepository: This is a Firestore permission error. Check security rules.")
            }
            // Fallback to local database only (which should already be saved)
            return weightDao.insertWeightEntry(weightEntry)
        }
    }

    // Function to test local database only
    suspend fun insertWeightEntryLocalOnly(weightEntry: WeightEntry): Long {
        println("DEBUG WeightRepository: Saving weight entry to local database only")
        return weightDao.insertWeightEntry(weightEntry)
    }

    // Function to get entries from local database only
    fun getAllWeightEntriesLocalOnly(userId: String): Flow<List<WeightEntry>> {
        println("DEBUG WeightRepository: Getting weight entries from local database only for user: $userId")
        return weightDao.getAllWeightEntries(userId)
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
