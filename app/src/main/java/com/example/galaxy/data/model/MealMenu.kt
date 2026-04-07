package com.example.galaxy.data.model

data class MealMenu(
    val cafeteria: String,
    val date: String,
    val dayOfWeek: String,
    val meals: List<Meal>,
)

data class Meal(
    val type: String,
    val time: String,
    val items: List<String>,
)
