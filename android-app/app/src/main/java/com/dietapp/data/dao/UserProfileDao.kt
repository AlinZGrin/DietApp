package com.dietapp.data.dao

import androidx.room.*
import com.dietapp.data.entities.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    fun getUserProfile(userId: String): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    suspend fun getUserProfileOnce(userId: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfile)

    @Update
    suspend fun updateUserProfile(userProfile: UserProfile)

    @Delete
    suspend fun deleteUserProfile(userProfile: UserProfile)

    @Query("UPDATE user_profiles SET currentWeight = :weight, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updateWeight(userId: String, weight: Double, updatedAt: java.util.Date)

    @Query("UPDATE user_profiles SET dailyCalorieGoal = :calories, dailyProteinGoal = :protein, dailyCarbGoal = :carbs, dailyFatGoal = :fat, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updateNutritionGoals(
        userId: String,
        calories: Int,
        protein: Double,
        carbs: Double,
        fat: Double,
        updatedAt: java.util.Date
    )
}
