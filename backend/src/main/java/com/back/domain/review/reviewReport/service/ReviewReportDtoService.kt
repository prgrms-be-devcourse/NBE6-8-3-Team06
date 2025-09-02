package com.back.domain.review.reviewReport.service

import com.back.domain.member.member.entity.Member
import com.back.domain.review.review.dto.ReviewDetailResponseDto
import com.back.domain.review.review.entity.Review
import com.back.domain.review.reviewReport.dto.ReviewReportCreateDto
import com.back.domain.review.reviewReport.dto.ReviewReportDetailResponseDto
import com.back.domain.review.reviewReport.dto.ReviewReportResponseDto
import com.back.domain.review.reviewReport.entity.ReviewReport
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class ReviewReportDtoService{

    fun entity2responseDto(reviewReport: ReviewReport): ReviewReportResponseDto {
        return ReviewReportResponseDto(
            id = reviewReport.id,
            createdDate = reviewReport.createDate,
            reason = reviewReport.reason,
            memberName = reviewReport.member.getName(),
            description = reviewReport.description,
            reportState = reviewReport.processed,
            reviewAuthor = reviewReport.review.member.getName(),
            bookName = reviewReport.review.book.title,
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
            description = reviewReportCreateDto.description,
        )
    }

    fun entity2detailResponseDto(reviewReport: ReviewReport, reviewDetailResponseDto: ReviewDetailResponseDto): ReviewReportDetailResponseDto{
        return ReviewReportDetailResponseDto(
            id = reviewReport.id,
            reason = reviewReport.reason,
            description = reviewReport.description,
            createdDate = reviewReport.createDate,
            reportState = reviewReport.processed,
            review = reviewDetailResponseDto,
            bookName = reviewReport.review.book.title,
            bookAuthor = reviewReport.review.book.authors.first().author.name,
            memberName = reviewReport.member.getName(),
        )
    }
}