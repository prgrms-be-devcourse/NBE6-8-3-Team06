package com.back.domain.review.reviewReport.controller

import com.back.domain.review.reviewReport.dto.ReviewReportCreateDto
import com.back.domain.review.reviewReport.service.ReviewReportService
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController("reviews/{review_id}/report")
class ReviewReportController(
    private val reviewReportService: ReviewReportService,
    private val rq: Rq,
) {

    @PostMapping
    fun createReviewReport(
        @PathVariable reviewId: Int,
        @RequestBody reviewReportCreateDto: ReviewReportCreateDto
    ): RsData<Void> {
        val member = rq.getAuthenticatedActor()
        reviewReportService.create(reviewId, member, reviewReportCreateDto)
        return RsData("201-1", "success to create review report")
    }
}