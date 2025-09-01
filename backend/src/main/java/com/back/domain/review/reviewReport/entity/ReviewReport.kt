package com.back.domain.review.reviewReport.entity

import com.back.domain.member.member.entity.Member
import com.back.domain.review.review.entity.Review
import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne

@Entity
data class ReviewReport(
    @ManyToOne
    var review: Review,
    @ManyToOne
    var member: Member,
    var reason: String,
): BaseEntity()