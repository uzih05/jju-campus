package com.example.galaxy.ui.screen.dashboard

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import java.time.LocalDate
import java.time.YearMonth
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.galaxy.data.model.Meal
import com.example.galaxy.data.remote.api.RoomStatus
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import androidx.compose.ui.layout.ContentScale

private val CardShape = RoundedCornerShape(16.dp)
private val CardBorder @Composable get() = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        // ━━ Header banner ━━
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surface,
                        ),
                    ),
                )
                .padding(horizontal = 20.dp, vertical = 28.dp),
        ) {
            if (state.userName != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("https://instar.jj.ac.kr/JSP/img_view_thumb.jsp?sabun=${state.userId ?: ""}")
                            .crossfade(true)
                            .build(),
                        contentDescription = "프로필",
                        modifier = Modifier.size(64.dp).clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            }
                        },
                        error = {
                            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("${state.userName}님", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "${state.userDept ?: ""} ${state.userYear ?: ""}학년",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                Column {
                    Text("Galaxy", style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(2.dp))
                    Text("전주대학교 캠퍼스", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // ━━ Bento grid content ━━
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            var showCalendarPopup by remember { mutableStateOf(false) }

            // Row 1: D-day (big) + Library seats (small)
            if (state.nextEvents.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Big D-day card — first event
                    val mainEvent = state.nextEvents.first()
                    val isSoon = mainEvent.daysLeft <= 7
                    Card(
                        modifier = Modifier.weight(1.4f).aspectRatio(0.95f).clickable { showCalendarPopup = true },
                        shape = CardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSoon) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        ),
                        elevation = CardDefaults.cardElevation(0.dp),
                        border = CardBorder,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.width(6.dp))
                                Text("다음 일정", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column {
                                Text(
                                    text = if (mainEvent.daysLeft == 0L) "TODAY" else "D-${mainEvent.daysLeft}",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSoon) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(mainEvent.label, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(mainEvent.dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Right column: small D-day cards stacked
                    if (state.nextEvents.size > 1) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            state.nextEvents.drop(1).take(2).forEach { event ->
                                val soon = event.daysLeft <= 7
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { showCalendarPopup = true },
                                    shape = CardShape,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(0.dp),
                                    border = CardBorder,
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = if (event.daysLeft == 0L) "TODAY" else "D-${event.daysLeft}",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (soon) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(event.label, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showCalendarPopup) {
                CalendarPopup(events = state.allEvents, onDismiss = { showCalendarPopup = false })
            }

            // Row 2: Library seats + Meal summary side by side
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Library seats mini card
                if (state.topRooms.isNotEmpty()) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = CardShape,
                        elevation = CardDefaults.cardElevation(0.dp),
                        border = CardBorder,
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EventSeat, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(6.dp))
                                Text("도서관 좌석", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(12.dp))
                            state.topRooms.take(3).forEach { room ->
                                val rate = if (room.activeTotal > 0) room.occupied.toFloat() / room.activeTotal else 0f
                                val color = when {
                                    rate > 0.9f -> MaterialTheme.colorScheme.error
                                    rate > 0.7f -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.primary
                                }
                                Row(
                                    modifier = Modifier.padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(room.name, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${room.available}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
                                }
                            }
                        }
                    }
                }

                // Meal summary mini card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = CardShape,
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = CardBorder,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(6.dp))
                            Text("오늘의 학식", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(12.dp))
                        if (state.todayMeals.isEmpty()) {
                            Text("식단 없음", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            state.todayMeals.take(3).forEach { meal ->
                                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                    Text(meal.type, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(36.dp))
                                    Text(
                                        meal.items.firstOrNull() ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Row 3: Recent notices — full width
            val context = LocalContext.current
            if (state.recentNotices.isNotEmpty()) {
                Card(
                    shape = CardShape,
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = CardBorder,
                ) {
                    Column {
                        Row(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(6.dp))
                            Text("최근 공지", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        state.recentNotices.forEachIndexed { index, notice ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(notice.url)))
                                }.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (notice.isPinned) {
                                    Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(Modifier.width(6.dp))
                                }
                                Text(notice.title, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            if (index < state.recentNotices.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CalendarPopup(events: List<CalendarEvent>, onDismiss: () -> Unit) {
    val today = LocalDate.now()
    val yearMonth = YearMonth.of(today.year, today.month)
    val daysInMonth = yearMonth.lengthOfMonth()
    val startOffset = (yearMonth.atDay(1).dayOfWeek.value - 1) % 7

    val eventDays = events.associate { it.date.dayOfMonth to it }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${yearMonth.year}년 ${yearMonth.monthValue}월", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("월", "화", "수", "목", "금", "토", "일").forEach { day ->
                        Text(
                            day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))

                for (week in 0..5) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0..6) {
                            val day = week * 7 + col - startOffset + 1
                            if (day in 1..daysInMonth) {
                                val isToday = day == today.dayOfMonth
                                val event = eventDays[day]
                                Box(
                                    modifier = Modifier.weight(1f).aspectRatio(1f).padding(1.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                isToday -> MaterialTheme.colorScheme.primary
                                                event != null -> MaterialTheme.colorScheme.primaryContainer
                                                else -> Color.Transparent
                                            },
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "$day", style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isToday || event != null) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        )
                                        if (event != null) {
                                            Text(
                                                event.label, style = MaterialTheme.typography.labelSmall,
                                                maxLines = 1, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center,
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                    if (week * 7 - startOffset + 1 > daysInMonth) break
                }

                Spacer(Modifier.height(12.dp))

                events.forEach { event ->
                    val dText = if (event.daysLeft == 0L) "D-Day"
                        else if (event.daysLeft > 0) "D-${event.daysLeft}"
                        else "D+${-event.daysLeft}"
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(
                            dText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                            color = if (event.daysLeft in 0..7) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(48.dp),
                        )
                        Text(event.label, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${event.date.monthValue}.${event.date.dayOfMonth}",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("닫기") } },
    )
}
