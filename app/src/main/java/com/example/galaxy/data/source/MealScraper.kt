package com.example.galaxy.data.source

import com.example.galaxy.data.model.Meal
import com.example.galaxy.data.model.MealMenu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class MealScraper {

    companion object {
        private const val MEAL_URL = "https://www.jj.ac.kr/jj/campuslife/food.do"
    }

    suspend fun fetchWeeklyMeals(): List<MealMenu> = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect(MEAL_URL)
            .timeout(10_000)
            .get()

        val cafeteriaName = doc.selectFirst(".h4-tit01")?.text() ?: "스타타워"
        val tabs = doc.select(".inner-tab li")
        val contents = doc.select(".inner-tab-content > div")

        val menus = mutableListOf<MealMenu>()

        for (i in tabs.indices) {
            val tabText = tabs[i].text().trim()
            val dayOfWeek = tabText.substringBefore(" ").trim()
            val date = tabText.substringAfter("(").substringBefore(")").trim()

            if (i >= contents.size) break
            val table = contents[i]
            val rows = table.select("tbody tr")

            val meals = rows.mapNotNull { row ->
                val th = row.selectFirst("th") ?: return@mapNotNull null
                val td = row.selectFirst("td.menu-text") ?: return@mapNotNull null

                val thText = th.text()
                val mealType = thText.substringBefore("(").trim()
                val mealTime = thText.substringAfter("운영시간:").substringBefore(")").trim()

                val menuText = td.html()
                    .replace("&lt;BR /&gt;", "\n")
                    .replace("&lt;br /&gt;", "\n")
                    .replace("<br>", "\n")
                    .replace("<BR />", "\n")
                    .trim()

                val items = menuText.split("\n")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                if (items.isEmpty()) null
                else Meal(type = mealType, time = mealTime, items = items)
            }

            menus.add(
                MealMenu(
                    cafeteria = cafeteriaName,
                    date = date,
                    dayOfWeek = dayOfWeek,
                    meals = meals,
                )
            )
        }

        menus
    }
}
