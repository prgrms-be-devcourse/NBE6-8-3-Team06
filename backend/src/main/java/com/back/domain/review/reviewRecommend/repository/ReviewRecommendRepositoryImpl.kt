package com.back.domain.review.reviewRecommend.repository

import com.back.domain.member.member.entity.Member
import com.back.domain.review.review.entity.Review
import com.back.domain.review.reviewRecommend.entity.QReviewRecommend
import com.querydsl.jpa.impl.JPAQueryFactory

class ReviewRecommendRepositoryImpl(
    private val queryFactory: JPAQueryFactory
):ReviewRecommendRepositoryCustom {
    override fun isRecommendedByReviewAndMember(
        review: Review,
        member: Member?
    ): Boolean? {
        if (member == null) return null
        return queryFactory.select(QReviewRecommend.reviewRecommend.isRecommended)
            .from(QReviewRecommend.reviewRecommend)
        .where(QReviewRecommend.reviewRecommend.review.eq(review), QReviewRecommend.reviewRecommend.member.eq(member))
            .fetchOne()
    }
}