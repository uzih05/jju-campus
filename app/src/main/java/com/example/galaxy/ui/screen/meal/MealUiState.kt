package com.example.galaxy.ui.screen.meal

import com.example.galaxy.data.model.MealMenu

sealed interface MealUiState {
    data object Loading : MealUiState
    data class Success(
        val weeklyMenus: List<MealMenu>,
        val selectedDayIndex: Int,
    ) : MealUiState
    data class Error(val message: String) : MealUiState
}
