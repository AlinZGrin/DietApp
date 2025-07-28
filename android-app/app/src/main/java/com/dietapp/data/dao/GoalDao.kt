package com.dietapp.data.dao

import androidx.room.*
import com.dietapp.data.entities.Goal
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC LIMIT 1")
    fun getCurrentGoal(userId: String): Flow<Goal?>

    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllGoals(userId: String): Flow<List<Goal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long

    @Update
    suspend fun updateGoal(goal: Goal)

    @Query("UPDATE goals SET isActive = 0 WHERE userId = :userId")
    suspend fun deactivateAllGoals(userId: String)

    @Delete
    suspend fun deleteGoal(goal: Goal)
}
