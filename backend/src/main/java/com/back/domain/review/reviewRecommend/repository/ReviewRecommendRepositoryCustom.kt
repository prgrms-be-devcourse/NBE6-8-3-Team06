package com.back.domain.review.reviewRecommend.repository

import com.back.domain.member.member.entity.Member
import com.back.domain.review.review.entity.Review

interface ReviewRecommendRepositoryCustom {
    fun isRecommendedByReviewAndMember(review: Review, member: Member?): Boolean?
}