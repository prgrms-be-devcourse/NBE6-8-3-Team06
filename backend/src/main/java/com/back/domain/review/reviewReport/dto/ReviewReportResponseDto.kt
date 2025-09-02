package com.back.domain.review.reviewReport.dto

import com.back.domain.review.reviewReport.entity.ReviewReportState
import java.time.LocalDateTime

data class ReviewReportResponseDto(
    val id: Int,
    val createdDate: LocalDateTime,
    val reason: String,
    val description: String,
    val memberName: String,
    val reportState: ReviewReportState,
    val reviewAuthor: String,
    val bookName: String,
)