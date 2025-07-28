package com.dietapp.data.dao

import androidx.room.*
import com.dietapp.data.entities.WeightEntry
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_entries WHERE userId = :userId ORDER BY date DESC")
    fun getAllWeightEntries(userId: String): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries WHERE userId = :userId ORDER BY date DESC LIMIT 1")
    fun getLatestWeightEntry(userId: String): Flow<WeightEntry?>

    @Query("SELECT * FROM weight_entries WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getWeightEntriesInRange(userId: String, startDate: Date, endDate: Date): Flow<List<WeightEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightEntry(weightEntry: WeightEntry): Long

    @Update
    suspend fun updateWeightEntry(weightEntry: WeightEntry)

    @Delete
    suspend fun deleteWeightEntry(weightEntry: WeightEntry)

    @Query("SELECT AVG(weight) FROM weight_entries WHERE userId = :userId AND date >= :startDate")
    suspend fun getAverageWeightSince(userId: String, startDate: Date): Double?
}
