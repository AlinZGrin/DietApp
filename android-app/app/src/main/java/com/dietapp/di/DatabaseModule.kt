package com.dietapp.di

import android.content.Context
import androidx.room.Room
import com.dietapp.data.database.DietAppDatabase
import com.dietapp.data.dao.*
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for database components
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDietAppDatabase(@ApplicationContext context: Context): DietAppDatabase {
        return Room.databaseBuilder(
            context,
            DietAppDatabase::class.java,
            DietAppDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration() // For development - remove in production
        .build()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    fun provideUserProfileDao(database: DietAppDatabase): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    fun provideWeightLogDao(database: DietAppDatabase): WeightLogDao {
        return database.weightLogDao()
    }

    @Provides
    fun provideFoodDao(database: DietAppDatabase): FoodDao {
        return database.foodDao()
    }

    @Provides
    fun provideFoodLogDao(database: DietAppDatabase): FoodLogDao {
        return database.foodLogDao()
    }

    @Provides
    fun provideExerciseLogDao(database: DietAppDatabase): ExerciseLogDao {
        return database.exerciseLogDao()
    }

    @Provides
    fun provideGoalDao(database: DietAppDatabase): GoalDao {
        return database.goalDao()
    }

    @Provides
    fun provideWeightDao(database: DietAppDatabase): WeightDao {
        return database.weightDao()
    }

    @Provides
    fun provideWaterIntakeDao(database: DietAppDatabase): WaterIntakeDao {
        return database.waterIntakeDao()
    }
}
