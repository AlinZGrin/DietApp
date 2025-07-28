package com.dietapp.data.dao

import androidx.room.*
import com.dietapp.data.entities.Food
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods WHERE id = :id")
    suspend fun getFoodById(id: String): Food?

    @Query("SELECT * FROM foods WHERE barcode = :barcode")
    suspend fun getFoodByBarcode(barcode: String): Food?

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchFoods(query: String): Flow<List<Food>>

    @Query("SELECT * FROM foods WHERE isCustom = 1 ORDER BY name ASC")
    fun getCustomFoods(): Flow<List<Food>>

    @Query("SELECT * FROM foods ORDER BY name ASC LIMIT :limit")
    fun getAllFoods(limit: Int = 100): Flow<List<Food>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: Food)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<Food>)

    @Update
    suspend fun updateFood(food: Food)

    @Delete
    suspend fun deleteFood(food: Food)

    @Query("DELETE FROM foods WHERE isCustom = 1")
    suspend fun deleteAllCustomFoods()
}
