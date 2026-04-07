package com.example.galaxy.ui.screen.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.galaxy.data.model.Meal
import com.example.galaxy.data.model.Notice
import com.example.galaxy.data.model.NoticeCategory
import com.example.galaxy.data.remote.LibraryClient
import com.example.galaxy.data.remote.MStarClient
import com.example.galaxy.data.remote.TokenStore
import com.example.galaxy.data.remote.api.RoomStatus
import com.example.galaxy.data.remote.api.SemesterDate
import com.example.galaxy.data.repository.MealRepository
import com.example.galaxy.data.repository.NoticeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar

data class NextEvent(val label: String, val dateStr: String, val daysLeft: Long)

data class CalendarEvent(val label: String, val date: LocalDate, val daysLeft: Long)

data class DashboardUiState(
    val userName: String? = null,
    val userId: String? = null,
    val userDept: String? = null,
    val userYear: String? = null,
    val nextEvents: List<NextEvent> = emptyList(),
    val allEvents: List<CalendarEvent> = emptyList(),
    val todayMeals: List<Meal> = emptyList(),
    val todayDate: String = "",
    val cafeteria: String = "",
    val recentNotices: List<Notice> = emptyList(),
    val topRooms: List<RoomStatus> = emptyList(),
    val isLoading: Boolean = true,
)

class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val mealRepo = MealRepository(app)
    private val noticeRepo = NoticeRepository(app)
    private val tokenStore = TokenStore(app)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init { load() }

    fun load() {
        _uiState.value = DashboardUiState(isLoading = true)
        viewModelScope.launch {
            val mealResult = mealRepo.getWeeklyMeals()
            val noticeResult = noticeRepo.getNotices(NoticeCategory.GENERAL)

            val rooms = try {
                LibraryClient.api.getRoomStatus().data?.list?.take(4) ?: emptyList()
            } catch (_: Exception) { emptyList() }

            val semesterData = try { MStarClient.api.getSemesterDates().data.firstOrNull() } catch (_: Exception) { null }
            val nextEvents = if (semesterData != null) parseNextEvents(semesterData) else emptyList()
            val allEvents = if (semesterData != null) parseAllEvents(semesterData) else emptyList()

            val dayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2).coerceIn(0, 4)
            val todayMenu = mealResult.getOrNull()?.getOrNull(dayIndex)

            _uiState.value = DashboardUiState(
                userName = tokenStore.getUserName(),
                userId = tokenStore.getUserId(),
                userDept = tokenStore.getUserDept(),
                userYear = tokenStore.getUserYear(),
                nextEvents = nextEvents,
                allEvents = allEvents,
                todayMeals = todayMenu?.meals ?: emptyList(),
                todayDate = todayMenu?.date ?: "",
                cafeteria = todayMenu?.cafeteria ?: "",
                recentNotices = noticeResult.getOrNull()?.take(5) ?: emptyList(),
                topRooms = rooms,
                isLoading = false,
            )
        }
    }

    private fun parseNextEvents(s: SemesterDate): List<NextEvent> {
        val today = LocalDate.now()
        val fmt = DateTimeFormatter.ofPattern("yyyyMMdd")
        val display = DateTimeFormatter.ofPattern("M.d (E)")
        val items = mutableListOf<NextEvent>()

        fun add(label: String, raw: String?) {
            if (raw.isNullOrBlank()) return
            try {
                val date = LocalDate.parse(raw, fmt)
                val days = ChronoUnit.DAYS.between(today, date)
                if (days >= 0) items.add(NextEvent(label, date.format(display), days))
            } catch (_: Exception) {}
        }

        add("개강", s.DATE_START)
        add("수강변경 마감", s.DATE_14)
        add("수강철회 마감", s.DATE_13)
        add("중간고사", s.DATE_MID_EXAM)
        add("기말고사", s.DATE_FIN_EXAM)
        add("종강", s.DATE_LAST)

        return items.sortedBy { it.daysLeft }.take(3)
    }

    private fun parseAllEvents(s: SemesterDate): List<CalendarEvent> {
        val today = LocalDate.now()
        val fmt = DateTimeFormatter.ofPattern("yyyyMMdd")
        val items = mutableListOf<CalendarEvent>()

        fun add(label: String, raw: String?) {
            if (raw.isNullOrBlank()) return
            try {
                val date = LocalDate.parse(raw, fmt)
                items.add(CalendarEvent(label, date, ChronoUnit.DAYS.between(today, date)))
            } catch (_: Exception) {}
        }

        add("개강", s.DATE_START)
        add("수강변경 마감", s.DATE_14)
        add("수강철회 마감", s.DATE_13)
        add("1/2선", s.DATE_12)
        add("2/3선", s.DATE_23)
        add("중간고사", s.DATE_MID_EXAM)
        add("기말고사", s.DATE_FIN_EXAM)
        add("종강", s.DATE_LAST)
        add("졸업", s.DATE_GRAD)

        return items.sortedBy { it.date }
    }
}
