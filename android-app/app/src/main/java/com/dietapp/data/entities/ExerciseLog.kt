package com.dietapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "exercise_logs")
data class ExerciseLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val exerciseName: String,
    val duration: Int, // in minutes
    val caloriesBurned: Double,
    val date: Date,
    val notes: String? = null,
    val createdAt: Date = Date()
)
