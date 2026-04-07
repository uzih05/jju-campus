package com.example.galaxy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_notices")
data class CachedNotice(
    @PrimaryKey val id: String,
    val category: String,
    val title: String,
    val url: String,
    val isNew: Boolean,
    val isPinned: Boolean,
    val hasAttachment: Boolean,
    val cachedAt: Long = System.currentTimeMillis(),
)
