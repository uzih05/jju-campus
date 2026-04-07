package com.example.galaxy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetable")
data class TimetableEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val professor: String = "",
    val room: String = "",
    val dayOfWeek: Int, // 0=월, 1=화, 2=수, 3=목, 4=금
    val startHour: Int,
    val startMinute: Int = 0,
    val endHour: Int,
    val endMinute: Int = 0,
    val color: Int = 0,
)
