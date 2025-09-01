package com.back.domain.review.reviewReport.service

import com.back.domain.member.member.entity.Member
import com.back.domain.review.review.service.ReviewService
import com.back.domain.review.reviewReport.dto.ReviewReportCreateDto
import com.back.domain.review.reviewReport.dto.ReviewReportProcessDto
import com.back.domain.review.reviewReport.dto.ReviewReportResponseDto
import com.back.domain.review.reviewReport.entity.ReviewReport
import com.back.domain.review.reviewReport.repository.ReviewReportRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewReportService(
    private val reviewReportRepository: ReviewReportRepository,
    private val reviewReportDtoService: ReviewReportDtoService,
    private val reviewService: ReviewService,
) {

    fun search(keyword: String?, pageable: Pageable, processed: Boolean): Page<ReviewReportResponseDto> {
        val reviewReportPage = reviewReportRepository.search(keyword, pageable, processed)
        return reviewReportDtoService.pageEntity2pageResponseDto(reviewReportPage);
    }

    @Transactional
    fun create(reviewId:Int, member: Member,reviewReportRequestDto: ReviewReportCreateDto) {
        val review = reviewService.findById(reviewId)?:throw NoSuchElementException("review not found")
        val reviewReport = reviewReportDtoService.createDto2entity(review, member, reviewReportRequestDto)
        reviewReportRepository.save(reviewReport)
    }

    @Transactional
    fun process(reportId: Int, reviewReportProcessDto: ReviewReportProcessDto){
        val reviewReport = reviewReportRepository.findById(reportId).orElseThrow { throw NoSuchElementException("Review report not found") }
        reviewReport.modify(reviewReportProcessDto)
    }

}