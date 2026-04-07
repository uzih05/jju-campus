package com.example.galaxy.data.repository

import android.content.Context
import com.example.galaxy.data.local.GalaxyDatabase
import com.example.galaxy.data.local.entity.CachedNotice
import com.example.galaxy.data.model.Notice
import com.example.galaxy.data.model.NoticeCategory
import com.example.galaxy.data.source.NoticeScraper

class NoticeRepository(context: Context? = null) {

    private val scraper = NoticeScraper()
    private val dao = context?.let { GalaxyDatabase.getInstance(it).noticeDao() }

    suspend fun getNotices(category: NoticeCategory, forceRefresh: Boolean = false): Result<List<Notice>> {
        if (!forceRefresh && dao != null) {
            val lastCached = dao.getLastCachedTime(category.name) ?: 0
            if (System.currentTimeMillis() - lastCached < 30 * 60 * 1000) {
                val cached = dao.getByCategory(category.name)
                if (cached.isNotEmpty()) return Result.success(cached.toNotices())
            }
        }

        return try {
            val notices = scraper.fetchNotices(category)
            dao?.let { d ->
                d.deleteByCategory(category.name)
                d.insertAll(notices.map { it.toCached(category) })
            }
            Result.success(notices)
        } catch (e: Exception) {
            val cached = dao?.getByCategory(category.name)
            if (!cached.isNullOrEmpty()) Result.success(cached.toNotices())
            else Result.failure(e)
        }
    }

    private fun Notice.toCached(category: NoticeCategory) = CachedNotice(
        id = id, category = category.name, title = title, url = url,
        isNew = isNew, isPinned = isPinned, hasAttachment = hasAttachment,
    )

    private fun List<CachedNotice>.toNotices() = map {
        Notice(it.id, it.title, it.url, it.isNew, it.isPinned, it.hasAttachment)
    }
}
