package com.dietapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val weight: Double, // in kg
    val bodyFatPercentage: Double? = null,
    val muscleMass: Double? = null,
    val date: Date = Date(),
    val notes: String? = null,
    val createdAt: Date = Date()
)
