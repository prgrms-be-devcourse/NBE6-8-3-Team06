package com.back.domain.review.reviewRecommend.entity

import com.back.domain.member.member.entity.Member
import com.back.domain.review.review.entity.Review
import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

@Entity
class ReviewRecommend(
    @field:ManyToOne(fetch = FetchType.LAZY) var review: Review,
    @field:ManyToOne(fetch = FetchType.LAZY) var member: Member,
    var isRecommended: Boolean
) : BaseEntity()
