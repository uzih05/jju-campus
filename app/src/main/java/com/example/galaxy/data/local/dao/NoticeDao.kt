package com.example.galaxy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.galaxy.data.local.entity.CachedNotice

@Dao
interface NoticeDao {
    @Query("SELECT * FROM cached_notices WHERE category = :category ORDER BY isPinned DESC")
    suspend fun getByCategory(category: String): List<CachedNotice>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notices: List<CachedNotice>)

    @Query("DELETE FROM cached_notices WHERE category = :category")
    suspend fun deleteByCategory(category: String)

    @Query("SELECT MAX(cachedAt) FROM cached_notices WHERE category = :category")
    suspend fun getLastCachedTime(category: String): Long?
}
