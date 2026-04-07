package com.example.galaxy.ui.screen.dashboard

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import java.time.LocalDate
import java.time.YearMonth
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
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
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.galaxy.data.model.Meal
import com.example.galaxy.data.remote.api.RoomStatus
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
    ) {
        // Header with profile
        if (state.userName != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://instar.jj.ac.kr/JSP/img_view_thumb.jsp?sabun=${state.userId ?: ""}")
                        .crossfade(true)
                        .build(),
                    contentDescription = "프로필",
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
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
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("${state.userName}님", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "${state.userDept ?: ""} ${state.userYear ?: ""}학년",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            Text("Galaxy", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("전주대학교 캠퍼스", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.height(16.dp))

        // Upcoming schedule - calendar style cards (clickable → popup)
        var showCalendarPopup by remember { mutableStateOf(false) }

        if (state.nextEvents.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                state.nextEvents.forEach { event ->
                    val isSoon = event.daysLeft <= 7
                    Card(
                        modifier = Modifier.weight(1f).aspectRatio(1f).clickable { showCalendarPopup = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSoon) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                        ),
                        elevation = CardDefaults.cardElevation(if (isSoon) 2.dp else 0.5.dp),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = if (event.daysLeft == 0L) "TODAY" else "D-${event.daysLeft}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSoon) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(event.label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(2.dp))
                            Text(event.dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // Calendar popup dialog
        if (showCalendarPopup) {
            CalendarPopup(events = state.allEvents, onDismiss = { showCalendarPopup = false })
        }

        // Library seats section
        if (state.topRooms.isNotEmpty()) {
            SectionHeader(Icons.Default.EventSeat, "도서관 좌석", "실시간")
            Spacer(Modifier.height(8.dp))
            state.topRooms.forEach { room ->
                MiniSeatCard(room)
                Spacer(Modifier.height(6.dp))
            }
            Spacer(Modifier.height(20.dp))
        }

        // Today's meal section
        SectionHeader(Icons.Default.Restaurant, "오늘의 학식", state.todayDate)
        Spacer(Modifier.height(8.dp))

        if (state.todayMeals.isEmpty()) {
            Text("등록된 식단이 없습니다", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            state.todayMeals.forEach { meal ->
                MiniMealCard(meal)
                Spacer(Modifier.height(6.dp))
            }
        }

        Spacer(Modifier.height(20.dp))

        // Recent notices section
        val context = LocalContext.current
        SectionHeader(Icons.Default.Notifications, "최근 공지", "일반")
        Spacer(Modifier.height(8.dp))

        if (state.recentNotices.isNotEmpty()) {
            Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(0.5.dp)) {
                Column {
                    state.recentNotices.forEachIndexed { index, notice ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(notice.url)))
                            }.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (notice.isPinned) {
                                Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(6.dp))
                            }
                            Text(notice.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        if (index < state.recentNotices.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MiniSeatCard(room: RoomStatus) {
    val rate = if (room.activeTotal > 0) room.occupied.toFloat() / room.activeTotal else 0f
    val color = when {
        rate > 0.9f -> MaterialTheme.colorScheme.error
        rate > 0.7f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    Card(shape = RoundedCornerShape(10.dp), elevation = CardDefaults.cardElevation(0.5.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(room.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text("${room.available}석", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
            Spacer(Modifier.width(8.dp))
            LinearProgressIndicator(
                progress = { rate },
                modifier = Modifier.width(60.dp).height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.15f),
            )
        }
    }
}

@Composable
private fun MiniMealCard(meal: Meal) {
    Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(0.5.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = when {
                        meal.type.contains("조식") -> Icons.Default.FreeBreakfast
                        meal.type.contains("중식") -> Icons.Default.LunchDining
                        else -> Icons.Default.DinnerDining
                    },
                    contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Row {
                    Text(meal.type, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    if (meal.time.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Text(meal.time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(meal.items.joinToString("  ·  "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CalendarPopup(events: List<CalendarEvent>, onDismiss: () -> Unit) {
    val today = LocalDate.now()
    val yearMonth = YearMonth.of(today.year, today.month)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value % 7 // 0=Sun ... 6=Sat → adjust to Mon=0
    val startOffset = (yearMonth.atDay(1).dayOfWeek.value - 1) % 7 // Mon=0

    val eventDays = events.associate { it.date.dayOfMonth to it }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "${yearMonth.year}년 ${yearMonth.monthValue}월",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                // Day headers
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("월", "화", "수", "목", "금", "토", "일").forEach { day ->
                        Text(
                            day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))

                // Calendar grid
                var dayCounter = 1
                for (week in 0..5) {
                    if (dayCounter > daysInMonth) break
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0..6) {
                            val cellDay = dayCounter - startOffset + (week * 7 + col - (if (week == 0) 0 else 0))
                            val day = week * 7 + col - startOffset + 1
                            if (day in 1..daysInMonth) {
                                val isToday = day == today.dayOfMonth
                                val event = eventDays[day]
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(1.dp)
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
                                            "$day",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isToday || event != null) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isToday -> MaterialTheme.colorScheme.onPrimary
                                                else -> MaterialTheme.colorScheme.onSurface
                                            },
                                        )
                                        if (event != null) {
                                            Text(
                                                event.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 7.sp,
                                                maxLines = 1,
                                                color = MaterialTheme.colorScheme.primary,
                                                textAlign = TextAlign.Center,
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Event list below calendar
                events.forEach { event ->
                    val dText = if (event.daysLeft == 0L) "D-Day"
                        else if (event.daysLeft > 0) "D-${event.daysLeft}"
                        else "D+${-event.daysLeft}"
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(
                            dText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (event.daysLeft in 0..7) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(48.dp),
                        )
                        Text(event.label, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${event.date.monthValue}.${event.date.dayOfMonth}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("닫기") }
        },
    )
}
