package com.back.domain.review.reviewReport.dto

import com.back.domain.review.reviewReport.entity.ReviewReportProcess

data class ReviewReportProcessDto(
    val process: ReviewReportProcess,
    val answer: String,
)
