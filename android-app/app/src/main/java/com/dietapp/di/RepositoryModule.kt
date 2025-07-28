package com.dietapp.di

import com.dietapp.auth.AuthRepository
import com.dietapp.data.api.OpenFoodFactsApiService
import com.dietapp.data.dao.*
import com.dietapp.data.repository.*
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return AuthRepository()
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        userProfileDao: UserProfileDao,
        weightLogDao: WeightLogDao
    ): UserRepository {
        return UserRepository(userProfileDao, weightLogDao)
    }

    @Provides
    @Singleton
    fun provideFoodRepository(
        foodDao: FoodDao,
        foodLogDao: FoodLogDao,
        firestore: FirebaseFirestore
    ): FoodRepository {
        return FoodRepository(foodDao, foodLogDao, firestore)
    }

    @Provides
    @Singleton
    fun provideGoalRepository(
        goalDao: GoalDao,
        firestore: FirebaseFirestore
    ): GoalRepository {
        return GoalRepository(goalDao, firestore)
    }

    @Provides
    @Singleton
    fun provideWeightRepository(
        weightDao: WeightDao,
        firestore: FirebaseFirestore
    ): WeightRepository {
        return WeightRepository(weightDao, firestore)
    }

    @Provides
    @Singleton
    fun provideWaterIntakeRepository(
        waterIntakeDao: WaterIntakeDao
    ): WaterIntakeRepository {
        return WaterIntakeRepository(waterIntakeDao)
    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenFoodFactsApiService(retrofit: Retrofit): OpenFoodFactsApiService {
        return retrofit.create(OpenFoodFactsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenFoodFactsRepository(openFoodFactsApiService: OpenFoodFactsApiService): OpenFoodFactsRepository {
        return OpenFoodFactsRepository(openFoodFactsApiService)
    }
}
