package com.example.galaxy.ui.screen.meal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.galaxy.data.repository.MealRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class MealViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = MealRepository(app)

    private val _uiState = MutableStateFlow<MealUiState>(MealUiState.Loading)
    val uiState: StateFlow<MealUiState> = _uiState

    init { loadMeals() }

    fun loadMeals() {
        _uiState.value = MealUiState.Loading
        viewModelScope.launch {
            repository.getWeeklyMeals()
                .onSuccess { menus ->
                    _uiState.value = MealUiState.Success(
                        weeklyMenus = menus,
                        selectedDayIndex = getTodayIndex(menus.size),
                    )
                }
                .onFailure { _uiState.value = MealUiState.Error(it.message ?: "식단을 불러올 수 없습니다") }
        }
    }

    fun selectDay(index: Int) {
        val current = _uiState.value
        if (current is MealUiState.Success) _uiState.value = current.copy(selectedDayIndex = index)
    }

    private fun getTodayIndex(totalDays: Int): Int {
        val index = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2
        return index.coerceIn(0, totalDays - 1)
    }
}
