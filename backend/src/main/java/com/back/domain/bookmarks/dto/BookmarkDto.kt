package com.back.domain.bookmarks.dto

import com.back.domain.bookmarks.entity.Bookmark
import com.back.domain.review.review.entity.Review
import java.time.LocalDateTime


data class BookmarkDto(
    val id: Int,
    val bookId: Int,
    val book: BookmarkBookDetailDto,
    val readState: String,
    val readPage: Int,
    val createDate: LocalDateTime,
    val startReadDate: LocalDateTime?,
    val endReadDate: LocalDateTime?,
    val readingRate: Double,
    val review: BookmarkReviewDetailDto?
) {
    constructor(bookmark: Bookmark, review: Review?) : this(
        bookmark.id,
        bookmark.book.id,
        BookmarkBookDetailDto(bookmark.book),
        bookmark.readState.toString(),
        bookmark.readPage,
        bookmark.createDate,
        bookmark.startReadDate,
        bookmark.endReadDate,
        bookmark.calculateReadingRate().toDouble(),
        if (review != null) BookmarkReviewDetailDto(review) else null
    )
}
