package com.dietapp.data.repository

import com.dietapp.data.dao.UserProfileDao
import com.dietapp.data.dao.WeightLogDao
import com.dietapp.data.entities.UserProfile
import com.dietapp.data.entities.WeightLog
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for user profile and weight tracking operations
 * Implements FR1.1, FR1.2, FR1.3, FR2.1, FR2.2, FR2.3, FR2.5, FR3.1, FR3.2
 */
@Singleton
class UserRepository @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val weightLogDao: WeightLogDao
) {

    // User Profile Operations (FR1.2, FR1.3)
    suspend fun getUserProfile(userId: String): UserProfile? {
        return userProfileDao.getUserProfileOnce(userId)
    }

    fun getUserProfileFlow(userId: String): Flow<UserProfile?> {
        return userProfileDao.getUserProfile(userId)
    }

    suspend fun insertUserProfile(profile: UserProfile) {
        userProfileDao.insertUserProfile(profile)
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        userProfileDao.updateUserProfile(profile)
    }

    suspend fun deleteUserProfile(profile: UserProfile) {
        userProfileDao.deleteUserProfile(profile)
    }

    // Weight tracking operations
    suspend fun insertWeightLog(weightLog: WeightLog) {
        weightLogDao.insertWeightLog(weightLog)
    }

    fun getWeightLogs(userId: String): Flow<List<WeightLog>> {
        return weightLogDao.getWeightLogs(userId)
    }

    suspend fun getLatestWeightLog(userId: String): WeightLog? {
        return weightLogDao.getLatestWeightLog(userId)
    }

    fun getRecentWeightLogs(userId: String, limit: Int = 30): Flow<List<WeightLog>> {
        return weightLogDao.getRecentWeightLogs(userId, limit)
    }
}
