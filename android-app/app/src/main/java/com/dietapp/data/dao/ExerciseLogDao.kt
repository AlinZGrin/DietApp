package com.dietapp.data.dao

import androidx.room.*
import com.dietapp.data.entities.ExerciseLog
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ExerciseLogDao {
    @Query("SELECT * FROM exercise_logs WHERE userId = :userId ORDER BY date DESC")
    fun getExerciseLogs(userId: String): Flow<List<ExerciseLog>>

    @Query("SELECT * FROM exercise_logs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExerciseLogsByDateRange(userId: String, startDate: Date, endDate: Date): Flow<List<ExerciseLog>>

    @Query("SELECT * FROM exercise_logs WHERE userId = :userId AND date(date/1000, 'unixepoch') = date(:date/1000, 'unixepoch') ORDER BY createdAt DESC")
    fun getExerciseLogsByDate(userId: String, date: Date): Flow<List<ExerciseLog>>

    @Query("SELECT SUM(caloriesBurned) FROM exercise_logs WHERE userId = :userId AND date(date/1000, 'unixepoch') = date(:date/1000, 'unixepoch')")
    suspend fun getTotalCaloriesBurnedForDate(userId: String, date: Date): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseLog(exerciseLog: ExerciseLog)

    @Update
    suspend fun updateExerciseLog(exerciseLog: ExerciseLog)

    @Delete
    suspend fun deleteExerciseLog(exerciseLog: ExerciseLog)

    @Query("DELETE FROM exercise_logs WHERE userId = :userId")
    suspend fun deleteAllExerciseLogs(userId: String)
}
