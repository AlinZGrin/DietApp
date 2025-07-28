package com.dietapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey
    val userId: String,
    val name: String,
    val email: String,
    val age: Int,
    val gender: String, // "Male", "Female", "Other"
    val height: Double, // in cm
    val currentWeight: Double, // in kg
    val targetWeight: Double, // in kg
    val activityLevel: String, // "Sedentary", "Light", "Moderate", "Active", "Very Active"
    val dietaryGoal: String, // "Weight Loss", "Weight Gain", "Maintain Weight", "Muscle Gain"
    val dailyCalorieGoal: Int,
    val dailyProteinGoal: Double, // in grams
    val dailyCarbGoal: Double, // in grams
    val dailyFatGoal: Double, // in grams
    val createdAt: Date,
    val updatedAt: Date
)
