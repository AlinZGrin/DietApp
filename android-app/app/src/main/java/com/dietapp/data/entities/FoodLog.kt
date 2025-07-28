package com.dietapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "food_logs",
    foreignKeys = [
        ForeignKey(
            entity = Food::class,
            parentColumns = ["id"],
            childColumns = ["foodId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FoodLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val foodId: String,
    val quantity: Double, // in grams or serving units
    val unit: String = "g", // "g", "ml", "cup", "piece", etc.
    val mealType: String, // "Breakfast", "Lunch", "Dinner", "Snack"
    val date: Date,
    val calories: Double, // calculated based on quantity
    val protein: Double, // calculated based on quantity
    val carbs: Double, // calculated based on quantity
    val fat: Double, // calculated based on quantity
    val createdAt: Date = Date()
)
