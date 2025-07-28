package com.dietapp.data.dao

import androidx.room.*
import com.dietapp.data.entities.WeightLog
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface WeightLogDao {
    @Query("SELECT * FROM weight_logs WHERE userId = :userId ORDER BY date DESC")
    fun getWeightLogs(userId: String): Flow<List<WeightLog>>

    @Query("SELECT * FROM weight_logs WHERE userId = :userId ORDER BY date DESC LIMIT :limit")
    fun getRecentWeightLogs(userId: String, limit: Int): Flow<List<WeightLog>>

    @Query("SELECT * FROM weight_logs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getWeightLogsByDateRange(userId: String, startDate: Date, endDate: Date): Flow<List<WeightLog>>

    @Query("SELECT * FROM weight_logs WHERE userId = :userId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestWeightLog(userId: String): WeightLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(weightLog: WeightLog)

    @Update
    suspend fun updateWeightLog(weightLog: WeightLog)

    @Delete
    suspend fun deleteWeightLog(weightLog: WeightLog)

    @Query("DELETE FROM weight_logs WHERE userId = :userId")
    suspend fun deleteAllWeightLogs(userId: String)
}
