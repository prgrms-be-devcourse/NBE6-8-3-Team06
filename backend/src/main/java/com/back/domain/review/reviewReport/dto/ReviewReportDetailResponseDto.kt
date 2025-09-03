package com.back.domain.review.reviewReport.dto

import com.back.domain.review.review.dto.ReviewDetailResponseDto
import com.back.domain.review.reviewReport.entity.ReviewReportState
import java.time.LocalDateTime

data class ReviewReportDetailResponseDto(
    val id: Int,
    val reason: String,
    val description: String,
    val memberName: String,
    val createdDate: LocalDateTime,
    val reportState: ReviewReportState,
    val review: ReviewDetailResponseDto,
    val bookName: String,
    val bookAuthor: String,
)