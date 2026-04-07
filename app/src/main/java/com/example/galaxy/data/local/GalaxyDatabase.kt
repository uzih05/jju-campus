package com.example.galaxy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.galaxy.data.local.dao.MealDao
import com.example.galaxy.data.local.dao.NoticeDao
import com.example.galaxy.data.local.dao.TimetableDao
import com.example.galaxy.data.local.entity.CachedMeal
import com.example.galaxy.data.local.entity.CachedNotice
import com.example.galaxy.data.local.entity.TimetableEntry

@Database(
    entities = [CachedMeal::class, CachedNotice::class, TimetableEntry::class],
    version = 2,
    exportSchema = false,
)
abstract class GalaxyDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun noticeDao(): NoticeDao
    abstract fun timetableDao(): TimetableDao

    companion object {
        @Volatile private var instance: GalaxyDatabase? = null

        fun getInstance(context: Context): GalaxyDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    GalaxyDatabase::class.java,
                    "galaxy.db",
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
        }
    }
}
