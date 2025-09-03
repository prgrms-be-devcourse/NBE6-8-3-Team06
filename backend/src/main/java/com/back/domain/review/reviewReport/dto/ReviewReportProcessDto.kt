package com.back.domain.review.reviewReport.dto

import com.back.domain.review.reviewReport.entity.ReviewReportState

data class ReviewReportProcessDto(
    val process: ReviewReportState,
    val answer: String,
)
