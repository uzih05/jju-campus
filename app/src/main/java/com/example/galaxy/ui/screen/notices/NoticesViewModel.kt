package com.example.galaxy.ui.screen.notices

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.galaxy.data.model.Notice
import com.example.galaxy.data.model.NoticeCategory
import com.example.galaxy.data.repository.NoticeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class NoticesUiState(
    val selectedCategory: NoticeCategory = NoticeCategory.GENERAL,
    val notices: List<Notice> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class NoticesViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = NoticeRepository(app)

    private val _uiState = MutableStateFlow(NoticesUiState())
    val uiState: StateFlow<NoticesUiState> = _uiState

    init { loadNotices(NoticeCategory.GENERAL) }

    fun selectCategory(category: NoticeCategory) {
        if (category == _uiState.value.selectedCategory && !_uiState.value.isLoading) return
        loadNotices(category)
    }

    fun refresh() { loadNotices(_uiState.value.selectedCategory) }

    private fun loadNotices(category: NoticeCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category, isLoading = true, error = null)
        viewModelScope.launch {
            repository.getNotices(category)
                .onSuccess { _uiState.value = _uiState.value.copy(notices = it, isLoading = false) }
                .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
        }
    }
}
