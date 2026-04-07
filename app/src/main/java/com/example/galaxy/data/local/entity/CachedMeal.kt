package com.example.galaxy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_meals")
data class CachedMeal(
    @PrimaryKey val id: String, // "date_mealType"
    val cafeteria: String,
    val date: String,
    val dayOfWeek: String,
    val mealType: String,
    val mealTime: String,
    val items: String, // joined by "|"
    val cachedAt: Long = System.currentTimeMillis(),
)
