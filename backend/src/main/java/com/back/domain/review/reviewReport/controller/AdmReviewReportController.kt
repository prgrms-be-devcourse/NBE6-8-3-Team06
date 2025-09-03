package com.back.domain.review.reviewReport.controller

import com.back.domain.review.reviewReport.dto.ReviewReportDetailResponseDto
import com.back.domain.review.reviewReport.dto.ReviewReportProcessDto
import com.back.domain.review.reviewReport.dto.ReviewReportResponseDto
import com.back.domain.review.reviewReport.service.ReviewReportService
import com.back.global.dto.PageResponseDto
import com.back.global.rsData.RsData
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/adm/reviews/report")
class AdmReviewReportController(
    private val reviewReportService: ReviewReportService
) {

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun search(
        @PageableDefault pageable: Pageable,
        @RequestParam keyword: String?,
        @RequestParam processed: Boolean = false,
    ):RsData<PageResponseDto<ReviewReportResponseDto>> {
        val reviewReportPage = reviewReportService.search(keyword, pageable, processed)
        return RsData("200-1", "review successfully searched", PageResponseDto(reviewReportPage))
    }

    @GetMapping("/{report_id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getReport(
        @PathVariable("report_id") reportId: Int,
    ):RsData<ReviewReportDetailResponseDto>{
        val reviewReport = reviewReportService.getReport(reportId)
        return RsData("200-1", "review successfully get", reviewReport)
    }

    @PutMapping("/{report_id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun process(
        @PathVariable("report_id") reportId: Int,
        @RequestBody reviewReportProcessDto: ReviewReportProcessDto
    ): RsData<Void>{
        reviewReportService.process(reportId, reviewReportProcessDto)
        return RsData("200-1", "review successfully proceed")
    }
}