package com.back.domain.review.reviewReport.dto

import java.time.LocalDateTime

data class ReviewReportResponseDto(
    val id: Int,
    val createdDate: LocalDateTime,
    val reason: String,
    val memberName: String,
)