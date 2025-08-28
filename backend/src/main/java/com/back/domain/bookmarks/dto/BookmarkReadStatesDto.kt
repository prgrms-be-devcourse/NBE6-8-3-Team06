package com.back.domain.bookmarks.dto


data class BookmarkReadStatesDto(
    val totalCount: Long,
    val avgRate: Double,
    val readState: ReadStateCount?
)
