package com.back.domain.review.reviewReport.controller

import com.back.domain.review.reviewReport.dto.ReviewReportResponseDto
import com.back.domain.review.reviewReport.service.ReviewReportService
import com.back.global.rsData.RsData
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("/adm/review/report")
class AdmReviewReportController(
    private val reviewReportService: ReviewReportService
) {

    @GetMapping
    fun search(
        @PageableDefault pageable: Pageable,
        @RequestParam keyword: String?,
        @RequestParam processed: Boolean = false,
    ):RsData<Page<ReviewReportResponseDto>> {
        val reviewReportPage = reviewReportService.search(keyword, pageable, processed)
        return RsData("200-1", "review successfully searched", reviewReportPage)
    }
}