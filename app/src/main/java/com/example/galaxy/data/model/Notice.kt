package com.example.galaxy.data.model

data class Notice(
    val id: String,
    val title: String,
    val url: String,
    val isNew: Boolean,
    val isPinned: Boolean,
    val hasAttachment: Boolean,
)

enum class NoticeCategory(val label: String, val path: String) {
    GENERAL("일반", "notice01.do"),
    ACADEMIC("학사", "notice02.do"),
    SCHOLARSHIP("장학", "notice03.do"),
    REGISTRATION("등록", "notice04.do"),
    PROCUREMENT("구매/입찰", "notice06.do"),
    RECRUITMENT("채용", "notice07.do"),
}
