package com.back.domain.bookmarks.dto

import com.back.domain.review.review.entity.Review
import java.time.LocalDateTime

data class BookmarkReviewDetailDto(
    val id: Int,
    val content: String,
    val rate: Double,
    val date: LocalDateTime
) {
    constructor(review: Review) : this(
        review.id,
        review.content,
        review.rate.toDouble(),
        review.modifyDate
    )
}
