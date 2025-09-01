package com.back.domain.review.reviewReport.service

import com.back.domain.member.member.entity.Member
import com.back.domain.review.review.entity.Review
import com.back.domain.review.reviewReport.dto.ReviewReportCreateDto
import com.back.domain.review.reviewReport.dto.ReviewReportResponseDto
import com.back.domain.review.reviewReport.entity.ReviewReport
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class ReviewReportDtoService {

    fun entity2responseDto(reviewReport: ReviewReport): ReviewReportResponseDto {
        return ReviewReportResponseDto(
            reviewReport.id,
            reviewReport.reason,
            reviewReport.review.member.getName()
        )
    }

    fun pageEntity2pageResponseDto(reviewReportPage: Page<ReviewReport>):Page<ReviewReportResponseDto>{
        return reviewReportPage.map { entity2responseDto(it) }
    }

    fun createDto2entity(review: Review, member: Member, reviewReportCreateDto: ReviewReportCreateDto): ReviewReport{
        return ReviewReport(
            review = review,
            member= member,
            reason = reviewReportCreateDto.reason,
        )
    }
}