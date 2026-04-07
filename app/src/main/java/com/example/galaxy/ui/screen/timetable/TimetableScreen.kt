package com.example.galaxy.ui.screen.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.galaxy.data.local.entity.TimetableEntry
import kotlinx.coroutines.launch

val CLASS_COLORS = listOf(
    Color(0xFF4285F4), Color(0xFF34A853), Color(0xFFFBBC04),
    Color(0xFFEA4335), Color(0xFF8E24AA), Color(0xFF00ACC1),
    Color(0xFFFF7043), Color(0xFF5C6BC0), Color(0xFF26A69A),
)

val DAYS = listOf("월", "화", "수", "목", "금")

// 전주대 교시 체계: 주1~주18 (09:00~18:00, 30분), 야1~야10 (18:00~23:00, 30분)
private data class Period(val label: String, val index: Int, val startH: Int, val startM: Int, val endH: Int, val endM: Int)

private val ALL_PERIODS: List<Period> = buildList {
    // 주1~주18: 09:00 base, 30min each
    for (n in 1..18) {
        val sh = 9 + (n - 1) / 2
        val sm = ((n - 1) % 2) * 30
        val eh = 9 + n / 2
        val em = (n % 2) * 30
        add(Period("주$n", size, sh, sm, eh, em))
    }
    // 야1~야10: 18:00 base, 30min each
    for (n in 1..10) {
        val sh = 18 + (n - 1) / 2
        val sm = ((n - 1) % 2) * 30
        val eh = 18 + n / 2
        val em = (n % 2) * 30
        add(Period("야$n", size, sh, sm, eh, em))
    }
}

// Grid: 09:00~23:00 = 14 hours, 30dp per 30min slot = 60dp/hour
private const val GRID_START_HOUR = 9
private const val GRID_END_HOUR = 23
private const val DP_PER_SLOT = 30 // 30dp per 30-minute slot
private const val TOTAL_SLOTS = (GRID_END_HOUR - GRID_START_HOUR) * 2 // 28 slots

private fun timeToOffset(hour: Int, minute: Int): Int {
    val totalMin = (hour - GRID_START_HOUR) * 60 + minute
    return (totalMin * DP_PER_SLOT / 30).coerceAtLeast(0)
}

private fun periodDropdownLabel(p: Period): String {
    return "${p.label} (%02d:%02d)".format(p.startH, p.startM)
}

private fun findPeriodByTime(hour: Int, minute: Int): Period? {
    return ALL_PERIODS.firstOrNull { it.startH == hour && it.startM == minute }
}

private fun periodRangeText(startH: Int, startM: Int, endH: Int, endM: Int): String {
    val sp = findPeriodByTime(startH, startM)
    // end period: find the period whose END matches
    val ep = ALL_PERIODS.lastOrNull { it.endH == endH && it.endM == endM }
    val pLabel = if (sp != null && ep != null) {
        if (sp.label == ep.label) sp.label else "${sp.label}~${ep.label}"
    } else ""
    return "$pLabel (%02d:%02d~%02d:%02d)".format(startH, startM, endH, endM)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(viewModel: TimetableViewModel = viewModel()) {
    val entries by viewModel.entries.collectAsState()
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedEntry by remember { mutableStateOf<TimetableEntry?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "수업 추가")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            Text(
                "시간표",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp),
            )

            // Timetable grid
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 4.dp),
            ) {
                // Period labels (show every hour + 주/야 boundary labels)
                Column(modifier = Modifier.width(36.dp)) {
                    Spacer(Modifier.height(28.dp)) // header
                    for (slot in 0 until TOTAL_SLOTS) {
                        val hour = GRID_START_HOUR + slot / 2
                        val isHour = slot % 2 == 0
                        Box(modifier = Modifier.height(DP_PER_SLOT.dp), contentAlignment = Alignment.TopEnd) {
                            if (isHour) {
                                val periodLabel = when {
                                    hour < 18 -> "주${(hour - 9) * 2 + 1}"
                                    else -> "야${(hour - 18) * 2 + 1}"
                                }
                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 2.dp)) {
                                    Text(
                                        "%02d".format(hour),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 9.sp,
                                    )
                                    Text(
                                        periodLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 8.sp,
                                    )
                                }
                            }
                        }
                    }
                }

                // Day columns
                DAYS.forEachIndexed { dayIndex, dayName ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    ) {
                        // Day header
                        Box(
                            modifier = Modifier.fillMaxWidth().height(28.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(dayName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }

                        // Time slots
                        Box(modifier = Modifier.fillMaxWidth().height((DP_PER_SLOT * TOTAL_SLOTS).dp)) {
                            // Grid lines (every hour)
                            for (h in GRID_START_HOUR..GRID_END_HOUR) {
                                val y = timeToOffset(h, 0)
                                val alpha = if (h == 18) 0.5f else 0.2f // 주/야 경계 강조
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.5.dp)
                                        .offset(y = y.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = alpha)),
                                )
                            }

                            // Class blocks
                            entries.filter { it.dayOfWeek == dayIndex }.forEach { entry ->
                                val startOff = timeToOffset(
                                    entry.startHour.coerceIn(GRID_START_HOUR, GRID_END_HOUR),
                                    entry.startMinute.coerceIn(0, 59),
                                )
                                val endOff = timeToOffset(
                                    entry.endHour.coerceIn(GRID_START_HOUR, GRID_END_HOUR),
                                    entry.endMinute.coerceIn(0, 59),
                                )
                                val blockHeight = (endOff - startOff).coerceAtLeast(DP_PER_SLOT / 2)
                                val color = CLASS_COLORS[entry.color % CLASS_COLORS.size]

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(blockHeight.dp)
                                        .offset(y = startOff.dp)
                                        .padding(1.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(color.copy(alpha = 0.9f), color.copy(alpha = 0.7f)),
                                            ),
                                        )
                                        .clickable { selectedEntry = entry }
                                        .padding(4.dp),
                                ) {
                                    Column {
                                        Text(
                                            entry.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 10.sp,
                                        )
                                        if (entry.room.isNotEmpty()) {
                                            Text(
                                                entry.room,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 9.sp,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add dialog
    if (showAddDialog) {
        AddClassDialog(
            existingCount = entries.size,
            onDismiss = { showAddDialog = false },
            onAdd = { entry ->
                scope.launch { viewModel.add(entry) }
                showAddDialog = false
            },
        )
    }

    // Detail/delete dialog
    if (selectedEntry != null) {
        val e = selectedEntry!!
        AlertDialog(
            onDismissRequest = { selectedEntry = null },
            title = { Text(e.name) },
            text = {
                Column {
                    if (e.professor.isNotEmpty()) Text("교수: ${e.professor}")
                    if (e.room.isNotEmpty()) Text("강의실: ${e.room}")
                    Text("${DAYS[e.dayOfWeek.coerceIn(0, 4)]} ${periodRangeText(e.startHour, e.startMinute, e.endHour, e.endMinute)}")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { viewModel.remove(e) }
                    selectedEntry = null
                }) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(4.dp))
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedEntry = null }) { Text("닫기") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddClassDialog(existingCount: Int, onDismiss: () -> Unit, onAdd: (TimetableEntry) -> Unit) {
    var name by remember { mutableStateOf("") }
    var professor by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var dayOfWeek by remember { mutableIntStateOf(0) }
    var startIndex by remember { mutableIntStateOf(0) }
    var endIndex by remember { mutableIntStateOf(0) }
    var dayExpanded by remember { mutableStateOf(false) }
    var startExpanded by remember { mutableStateOf(false) }
    var endExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("수업 추가") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("수업명") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = professor, onValueChange = { professor = it }, label = { Text("교수명") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("강의실") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                // Day picker
                ExposedDropdownMenuBox(expanded = dayExpanded, onExpandedChange = { dayExpanded = it }) {
                    OutlinedTextField(
                        value = DAYS[dayOfWeek],
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("요일") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dayExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(expanded = dayExpanded, onDismissRequest = { dayExpanded = false }) {
                        DAYS.forEachIndexed { i, d ->
                            DropdownMenuItem(text = { Text(d) }, onClick = { dayOfWeek = i; dayExpanded = false })
                        }
                    }
                }

                // Period pickers
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Start period
                    ExposedDropdownMenuBox(
                        expanded = startExpanded,
                        onExpandedChange = { startExpanded = it },
                        modifier = Modifier.weight(1f),
                    ) {
                        OutlinedTextField(
                            value = ALL_PERIODS[startIndex].label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("시작") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(startExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        )
                        ExposedDropdownMenu(expanded = startExpanded, onDismissRequest = { startExpanded = false }) {
                            ALL_PERIODS.forEachIndexed { i, p ->
                                DropdownMenuItem(
                                    text = { Text(periodDropdownLabel(p)) },
                                    onClick = {
                                        startIndex = i
                                        if (endIndex < i) endIndex = i
                                        startExpanded = false
                                    },
                                )
                            }
                        }
                    }

                    // End period
                    ExposedDropdownMenuBox(
                        expanded = endExpanded,
                        onExpandedChange = { endExpanded = it },
                        modifier = Modifier.weight(1f),
                    ) {
                        OutlinedTextField(
                            value = ALL_PERIODS[endIndex].label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("종료") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(endExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        )
                        ExposedDropdownMenu(expanded = endExpanded, onDismissRequest = { endExpanded = false }) {
                            for (i in startIndex until ALL_PERIODS.size) {
                                val p = ALL_PERIODS[i]
                                DropdownMenuItem(
                                    text = { Text(periodDropdownLabel(p)) },
                                    onClick = { endIndex = i; endExpanded = false },
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val sp = ALL_PERIODS[startIndex]
                        val ep = ALL_PERIODS[endIndex]
                        onAdd(TimetableEntry(
                            name = name, professor = professor, room = room,
                            dayOfWeek = dayOfWeek,
                            startHour = sp.startH, startMinute = sp.startM,
                            endHour = ep.endH, endMinute = ep.endM,
                            color = existingCount % CLASS_COLORS.size,
                        ))
                    }
                },
            ) { Text("추가") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
