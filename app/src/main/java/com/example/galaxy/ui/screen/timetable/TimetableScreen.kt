package com.example.galaxy.ui.screen.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

// 교시 정의: 1교시=09:00~09:50, 2교시=10:00~10:50, ...
private const val FIRST_HOUR = 9
private const val TOTAL_PERIODS = 13

private fun periodStartHour(period: Int) = FIRST_HOUR + period - 1
private fun hourToPeriod(hour: Int) = (hour - FIRST_HOUR + 1).coerceIn(1, TOTAL_PERIODS)
private fun periodLabel(period: Int): String {
    val h = periodStartHour(period)
    return "${period}교시 (%02d:00)".format(h)
}
private fun periodRangeLabel(startPeriod: Int, endPeriod: Int): String {
    val sh = periodStartHour(startPeriod)
    val eh = periodStartHour(endPeriod)
    return if (startPeriod == endPeriod) "${startPeriod}교시 (%02d:00~%02d:50)".format(sh, eh)
    else "${startPeriod}~${endPeriod}교시 (%02d:00~%02d:50)".format(sh, eh)
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
                    .padding(horizontal = 8.dp),
            ) {
                // Period labels
                Column(modifier = Modifier.width(32.dp)) {
                    Spacer(Modifier.height(28.dp)) // header
                    for (period in 1..TOTAL_PERIODS) {
                        Box(modifier = Modifier.height(48.dp), contentAlignment = Alignment.TopEnd) {
                            Text(
                                "$period",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 4.dp),
                            )
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
                        Box(modifier = Modifier.fillMaxWidth().height((48 * 13).dp)) {
                            // Grid lines
                            for (hour in 9..21) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.5.dp)
                                        .offset(y = ((hour - 9) * 48).dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                                )
                            }

                            // Class blocks (clamp to valid range)
                            entries.filter { it.dayOfWeek == dayIndex }.forEach { entry ->
                                val clampedStartH = entry.startHour.coerceIn(FIRST_HOUR, FIRST_HOUR + TOTAL_PERIODS - 1)
                                val clampedStartM = entry.startMinute.coerceIn(0, 59)
                                val clampedEndH = entry.endHour.coerceIn(clampedStartH, FIRST_HOUR + TOTAL_PERIODS)
                                val clampedEndM = entry.endMinute.coerceIn(0, 59)
                                val topOffset = ((clampedStartH - FIRST_HOUR) * 48 + clampedStartM * 48 / 60).dp
                                val blockHeight = ((clampedEndH - clampedStartH) * 48 + (clampedEndM - clampedStartM) * 48 / 60).coerceAtLeast(24).dp
                                val color = CLASS_COLORS[entry.color % CLASS_COLORS.size]

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(blockHeight)
                                        .offset(y = topOffset)
                                        .padding(1.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(color.copy(alpha = 0.85f))
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
        AlertDialog(
            onDismissRequest = { selectedEntry = null },
            title = { Text(selectedEntry!!.name) },
            text = {
                Column {
                    if (selectedEntry!!.professor.isNotEmpty()) Text("교수: ${selectedEntry!!.professor}")
                    if (selectedEntry!!.room.isNotEmpty()) Text("강의실: ${selectedEntry!!.room}")
                    val sp = hourToPeriod(selectedEntry!!.startHour)
                    val ep = hourToPeriod(selectedEntry!!.endHour)
                    Text("${DAYS[selectedEntry!!.dayOfWeek.coerceIn(0, 4)]} ${periodRangeLabel(sp, ep)}")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { viewModel.remove(selectedEntry!!) }
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
    var startPeriod by remember { mutableIntStateOf(1) }
    var endPeriod by remember { mutableIntStateOf(1) }
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
                            value = "${startPeriod}교시",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("시작") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(startExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        )
                        ExposedDropdownMenu(expanded = startExpanded, onDismissRequest = { startExpanded = false }) {
                            for (p in 1..TOTAL_PERIODS) {
                                DropdownMenuItem(
                                    text = { Text(periodLabel(p)) },
                                    onClick = {
                                        startPeriod = p
                                        if (endPeriod < p) endPeriod = p
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
                            value = "${endPeriod}교시",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("종료") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(endExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        )
                        ExposedDropdownMenu(expanded = endExpanded, onDismissRequest = { endExpanded = false }) {
                            for (p in startPeriod..TOTAL_PERIODS) {
                                DropdownMenuItem(
                                    text = { Text(periodLabel(p)) },
                                    onClick = { endPeriod = p; endExpanded = false },
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
                        onAdd(TimetableEntry(
                            name = name, professor = professor, room = room,
                            dayOfWeek = dayOfWeek,
                            startHour = periodStartHour(startPeriod), startMinute = 0,
                            endHour = periodStartHour(endPeriod), endMinute = 50,
                            color = existingCount % CLASS_COLORS.size,
                        ))
                    }
                },
            ) { Text("추가") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
