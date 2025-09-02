package com.back.domain.review.reviewReport.entity

import com.back.domain.member.member.entity.Member
import com.back.domain.review.review.entity.Review
import com.back.domain.review.reviewReport.dto.ReviewReportProcessDto
import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.ManyToOne

@Entity
data class ReviewReport(
    @ManyToOne
    var review: Review,
    @ManyToOne
    var member: Member,
    var reason: String,
    var description: String,
): BaseEntity(){

    var answer: String? = null
    @Enumerated(EnumType.STRING)
    var processed: ReviewReportState = ReviewReportState.PENDING

    fun modify(reviewReportProcessDto: ReviewReportProcessDto) {
        this.processed = reviewReportProcessDto.process
        this.answer = reviewReportProcessDto.answer
    }
}