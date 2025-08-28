package com.back.domain.bookmarks.dto

import java.time.LocalDateTime


data class BookmarkModifyRequestDto(
    val readState: String,
    val startReadDate: LocalDateTime?,
    val endReadDate: LocalDateTime?,
    val readPage: Int
)
