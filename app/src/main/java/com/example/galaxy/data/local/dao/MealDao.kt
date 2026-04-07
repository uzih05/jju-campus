package com.example.galaxy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.galaxy.data.local.entity.CachedMeal

@Dao
interface MealDao {
    @Query("SELECT * FROM cached_meals ORDER BY date, mealType")
    suspend fun getAll(): List<CachedMeal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(meals: List<CachedMeal>)

    @Query("DELETE FROM cached_meals")
    suspend fun deleteAll()

    @Query("SELECT MAX(cachedAt) FROM cached_meals")
    suspend fun getLastCachedTime(): Long?
}
