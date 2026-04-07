package com.example.galaxy.ui.screen.schedule

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.galaxy.data.remote.MStarClient
import com.example.galaxy.data.remote.TokenStore
import com.example.galaxy.data.remote.api.SemesterDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class DDay(val label: String, val date: LocalDate, val daysLeft: Long)

data class ScheduleUiState(
    val isLoading: Boolean = true,
    val ddays: List<DDay> = emptyList(),
    val error: String? = null,
)

class ScheduleViewModel(app: Application) : AndroidViewModel(app) {
    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState

    init { loadSemester() }

    fun refresh() { loadSemester() }

    private fun loadSemester() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val response = MStarClient.api.getSemesterDates()
                val semester = response.data.firstOrNull()
                if (semester != null) {
                    _uiState.value = _uiState.value.copy(
                        ddays = parseDDays(semester),
                        isLoading = false,
                    )
                } else {
                    _uiState.value = _uiState.value.copy(ddays = emptyList(), isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun parseDDays(s: SemesterDate): List<DDay> {
        val today = LocalDate.now()
        val fmt = DateTimeFormatter.ofPattern("yyyyMMdd")
        val items = mutableListOf<DDay>()

        fun add(label: String, raw: String?) {
            if (raw.isNullOrBlank()) return
            try {
                val date = LocalDate.parse(raw, fmt)
                val days = ChronoUnit.DAYS.between(today, date)
                items.add(DDay(label, date, days))
            } catch (_: Exception) {}
        }

        add("개강일", s.DATE_START)
        add("1/4선 (수강 변경 마감)", s.DATE_14)
        add("1/3선 (수강 철회 마감)", s.DATE_13)
        add("1/2선", s.DATE_12)
        add("2/3선", s.DATE_23)
        add("중간고사", s.DATE_MID_EXAM)
        add("기말고사", s.DATE_FIN_EXAM)
        add("종강일", s.DATE_LAST)
        add("졸업일", s.DATE_GRAD)

        return items.sortedBy { it.date }
    }
}

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 16.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("학사일정", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = { viewModel.refresh() }) {
                Icon(Icons.Default.Refresh, contentDescription = "새로고침")
            }
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }
            state.ddays.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("학기 정보가 없습니다", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.ddays) { dday ->
                        DDayCard(dday)
                    }
                }
            }
        }
    }
}

@Composable
private fun DDayCard(dday: DDay) {
    val isPast = dday.daysLeft < 0
    val isToday = dday.daysLeft == 0L
    val isSoon = dday.daysLeft in 1..7

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isToday -> MaterialTheme.colorScheme.primaryContainer
                isSoon -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surface
            },
        ),
        elevation = CardDefaults.cardElevation(if (isPast) 0.dp else 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // D-Day badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        when {
                            isToday -> MaterialTheme.colorScheme.primary
                            isSoon -> MaterialTheme.colorScheme.error
                            isPast -> MaterialTheme.colorScheme.outlineVariant
                            else -> MaterialTheme.colorScheme.primaryContainer
                        },
                        RoundedCornerShape(12.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = when {
                        isToday -> "D-Day"
                        dday.daysLeft > 0 -> "D-${dday.daysLeft}"
                        else -> "D+${-dday.daysLeft}"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isToday || isSoon -> MaterialTheme.colorScheme.onPrimary
                        isPast -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.primary
                    },
                    fontSize = if (dday.daysLeft.toString().length > 2) 11.sp else 13.sp,
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    dday.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPast) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    dday.date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd (E)")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
