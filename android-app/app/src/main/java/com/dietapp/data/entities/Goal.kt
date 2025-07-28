package com.dietapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val goalType: String, // "weight_loss", "weight_gain", "muscle_gain", "maintenance"
    val targetWeight: Double? = null,
    val targetCalories: Double,
    val targetProtein: Double,
    val targetCarbs: Double,
    val targetFat: Double,
    val targetDate: Date? = null,
    val weeklyWeightLossGoal: Double = 0.0, // kg per week
    val activityLevel: String = "moderate", // "sedentary", "light", "moderate", "active", "very_active"
    val currentHeight: Double? = null, // in cm
    val useImperialUnits: Boolean = false, // true for lbs/ft+in, false for kg/cm
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
