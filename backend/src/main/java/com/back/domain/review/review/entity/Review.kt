package com.back.domain.review.review.entity

import com.back.domain.book.book.entity.Book
import com.back.domain.member.member.entity.Member
import com.back.domain.review.reviewRecommend.entity.ReviewRecommend
import com.back.domain.review.reviewReport.dto.ReviewReportProcessDto
import com.back.domain.review.reviewReport.entity.ReviewReport
import com.back.domain.review.reviewReport.entity.ReviewReportState
import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.*

@Entity
class Review //    @Version
//    private Long version;
    (
    var content: String,
    var rate: Int,
    var spoiler: Boolean,
    @field:ManyToOne(fetch = FetchType.LAZY)
    var member: Member,
    @field:ManyToOne(fetch = FetchType.LAZY)
    var book: Book
) : BaseEntity() {

    var deleted = false

    var likeCount = 0

    var dislikeCount = 0

    @Enumerated(EnumType.STRING)
    var reportState: ReviewReportState = ReviewReportState.NOT_REPORTED
    var adminMessage: String? = null

    fun incLike() {
        this.likeCount++
    }

    fun decLike() {
        this.likeCount--
    }

    fun incDislike() {
        this.dislikeCount++
    }

    fun decDislike() {
        this.dislikeCount--
    }

    fun updateReport(reviewReportProcessDto: ReviewReportProcessDto) {
        this.reportState = reviewReportProcessDto.process
        this.adminMessage = reviewReportProcessDto.answer
    }
}
