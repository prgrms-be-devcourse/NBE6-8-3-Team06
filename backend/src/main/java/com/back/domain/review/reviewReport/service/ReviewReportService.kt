package com.back.domain.review.reviewReport.service

import com.back.domain.member.member.entity.Member
import com.back.domain.review.review.service.ReviewService
import com.back.domain.review.reviewReport.dto.ReviewReportCreateDto
import com.back.domain.review.reviewReport.dto.ReviewReportResponseDto
import com.back.domain.review.reviewReport.entity.ReviewReport
import com.back.domain.review.reviewReport.repository.ReviewReportRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ReviewReportService(
    private val reviewReportRepository: ReviewReportRepository,
    private val reviewReportDtoService: ReviewReportDtoService,
    private val reviewService: ReviewService,
) {

    fun search(keyword: String?, pageable: Pageable): Page<ReviewReportResponseDto> {
        val reviewReportPage = reviewReportRepository.search(keyword, pageable)
        return reviewReportDtoService.pageEntity2pageResponseDto(reviewReportPage);
    }

    @Transactional
    fun create(reviewId:Int, member: Member,reviewReportRequestDto: ReviewReportCreateDto) {
        val review = reviewService.findById(reviewId).get()
        val reviewReport = reviewReportDtoService.createDto2entity(review, member, reviewReportRequestDto)
        reviewReportRepository.save(reviewReport)
    }
}