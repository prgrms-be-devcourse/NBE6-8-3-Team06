package com.back.domain.bookmarks.dto

import com.back.domain.review.review.entity.Review
import com.back.domain.review.reviewReport.entity.ReviewReportState
import java.time.LocalDateTime

data class BookmarkReviewDetailDto(
    val id: Int,
    val content: String,
    val rate: Double,
    val date: LocalDateTime,
    val reportState: ReviewReportState,
    val adminMessage: String?
) {
    constructor(review: Review) : this(
        id = review.id,
        content = review.content,
        rate = review.rate.toDouble(),
        date = review.modifyDate,
        reportState = review.reportState,
        adminMessage = review.adminMessage,
    )
}
