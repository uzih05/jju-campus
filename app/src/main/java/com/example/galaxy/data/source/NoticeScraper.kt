package com.example.galaxy.data.source

import com.example.galaxy.data.model.Notice
import com.example.galaxy.data.model.NoticeCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class NoticeScraper {

    companion object {
        private const val BASE_URL = "https://www.jj.ac.kr/jj/community/"
    }

    suspend fun fetchNotices(category: NoticeCategory): List<Notice> = withContext(Dispatchers.IO) {
        val url = BASE_URL + category.path
        val doc = Jsoup.connect(url).timeout(10_000).get()
        val rows = doc.select("table.board-table tbody tr")

        rows.mapNotNull { row ->
            val titleLink = row.selectFirst(".b-title-box a") ?: return@mapNotNull null
            val articleNo = titleLink.attr("data-article-no")
            val title = titleLink.text().trim()
            if (title.isEmpty()) return@mapNotNull null

            val href = titleLink.attr("href")
            val fullUrl = if (href.startsWith("http")) href
                else "${BASE_URL}${category.path}$href"

            val isPinned = row.hasClass("b-noti-box")
            val isNew = row.selectFirst(".b-new") != null
            val hasAttachment = row.selectFirst(".b-file") != null

            Notice(
                id = articleNo,
                title = title,
                url = fullUrl,
                isNew = isNew,
                isPinned = isPinned,
                hasAttachment = hasAttachment,
            )
        }
    }
}
