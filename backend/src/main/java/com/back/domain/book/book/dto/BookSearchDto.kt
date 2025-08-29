package com.back.domain.book.book.dto

import com.back.domain.bookmarks.constant.ReadState
import java.time.LocalDateTime

data class BookSearchDto(
    val id: Long = 0,
    val title: String? = null,
    val imageUrl: String? = null,
    val publisher: String? = null,
    val isbn13: String? = null,
    val totalPage: Int = 0,
    val publishedDate: LocalDateTime? = null,
    val avgRate: Float = 0f,
    val categoryName: String? = null,
    val authors: List<String> = emptyList(),
    val readState: ReadState? = null
)