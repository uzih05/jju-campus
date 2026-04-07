package com.example.galaxy.ui.screen.timetable

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.galaxy.data.local.GalaxyDatabase
import com.example.galaxy.data.local.entity.TimetableEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class TimetableViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = GalaxyDatabase.getInstance(app).timetableDao()
    val entries = dao.getAll().stateIn(
        scope = CoroutineScope(Dispatchers.IO),
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )
    suspend fun add(entry: TimetableEntry) = dao.insert(entry)
    suspend fun remove(entry: TimetableEntry) = dao.delete(entry)
}
