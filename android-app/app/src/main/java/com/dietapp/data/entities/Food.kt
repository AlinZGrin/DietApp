package com.dietapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "foods")
data class Food(
    @PrimaryKey
    val id: String, // Can be barcode or generated ID
    val name: String,
    val brand: String? = null,
    val calories: Double, // per 100g
    val protein: Double, // per 100g
    val carbs: Double, // per 100g
    val fat: Double, // per 100g
    val fiber: Double? = null, // per 100g
    val sugar: Double? = null, // per 100g
    val sodium: Double? = null, // per 100g in mg
    val barcode: String? = null,
    val servingSize: Double? = null, // in grams
    val servingUnit: String? = "g",
    val createdAt: Date = Date(),
    val isCustom: Boolean = false // true if user-created
)
