package com.example.galaxy.data.repository

import android.content.Context
import com.example.galaxy.data.local.GalaxyDatabase
import com.example.galaxy.data.local.entity.CachedMeal
import com.example.galaxy.data.model.Meal
import com.example.galaxy.data.model.MealMenu
import com.example.galaxy.data.source.MealScraper

class MealRepository(context: Context? = null) {

    private val scraper = MealScraper()
    private val dao = context?.let { GalaxyDatabase.getInstance(it).mealDao() }

    suspend fun getWeeklyMeals(forceRefresh: Boolean = false): Result<List<MealMenu>> {
        if (!forceRefresh && dao != null) {
            val lastCached = dao.getLastCachedTime() ?: 0
            if (System.currentTimeMillis() - lastCached < 2 * 60 * 60 * 1000) {
                val cached = dao.getAll()
                if (cached.isNotEmpty()) return Result.success(cached.toMealMenus())
            }
        }

        return try {
            val menus = scraper.fetchWeeklyMeals()
            dao?.let { d ->
                d.deleteAll()
                d.insertAll(menus.toCachedMeals())
            }
            Result.success(menus)
        } catch (e: Exception) {
            // Fallback to cache on error
            val cached = dao?.getAll()
            if (!cached.isNullOrEmpty()) Result.success(cached.toMealMenus())
            else Result.failure(e)
        }
    }

    private fun List<MealMenu>.toCachedMeals(): List<CachedMeal> = flatMap { menu ->
        menu.meals.map { meal ->
            CachedMeal(
                id = "${menu.date}_${meal.type}",
                cafeteria = menu.cafeteria,
                date = menu.date,
                dayOfWeek = menu.dayOfWeek,
                mealType = meal.type,
                mealTime = meal.time,
                items = meal.items.joinToString("|"),
            )
        }
    }

    private fun List<CachedMeal>.toMealMenus(): List<MealMenu> =
        groupBy { it.date }.map { (_, meals) ->
            val first = meals.first()
            MealMenu(
                cafeteria = first.cafeteria,
                date = first.date,
                dayOfWeek = first.dayOfWeek,
                meals = meals.map { Meal(it.mealType, it.mealTime, it.items.split("|")) },
            )
        }
}
