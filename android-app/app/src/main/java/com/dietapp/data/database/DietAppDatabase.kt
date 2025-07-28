package com.dietapp.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.dietapp.data.converters.DateConverters
import com.dietapp.data.dao.*
import com.dietapp.data.entities.*

@Database(
    entities = [
        UserProfile::class,
        WeightLog::class,
        Food::class,
        FoodLog::class,
        ExerciseLog::class,
        Goal::class,
        WeightEntry::class,
        WaterIntake::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class DietAppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun weightLogDao(): WeightLogDao
    abstract fun foodDao(): FoodDao
    abstract fun foodLogDao(): FoodLogDao
    abstract fun exerciseLogDao(): ExerciseLogDao
    abstract fun goalDao(): GoalDao
    abstract fun weightDao(): WeightDao
    abstract fun waterIntakeDao(): WaterIntakeDao

    companion object {
        const val DATABASE_NAME = "diet_app_database"
    }
}
