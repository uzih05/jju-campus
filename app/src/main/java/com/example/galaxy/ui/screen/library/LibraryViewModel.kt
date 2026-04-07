package com.example.galaxy.ui.screen.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.galaxy.data.remote.LibraryClient
import com.example.galaxy.data.remote.api.PopularBook
import com.example.galaxy.data.remote.api.RoomStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LibraryUiState(
    val rooms: List<RoomStatus> = emptyList(),
    val books: List<PopularBook> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class LibraryViewModel(app: Application) : AndroidViewModel(app) {
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState

    init { load() }

    fun load() {
        _uiState.value = LibraryUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val rooms = LibraryClient.api.getRoomStatus()
                val books = LibraryClient.api.getPopularBooks(10)
                _uiState.value = LibraryUiState(
                    rooms = rooms.data?.list ?: emptyList(),
                    books = books.data?.list ?: emptyList(),
                    isLoading = false,
                )
            } catch (e: Exception) {
                _uiState.value = LibraryUiState(isLoading = false, error = e.message)
            }
        }
    }
}
