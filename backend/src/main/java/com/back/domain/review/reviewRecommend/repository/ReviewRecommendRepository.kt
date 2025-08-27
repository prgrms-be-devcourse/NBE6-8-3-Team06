package com.back.domain.review.reviewRecommend.repository

import com.back.domain.member.member.entity.Member
import com.back.domain.review.review.entity.Review
import com.back.domain.review.reviewRecommend.entity.ReviewRecommend
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ReviewRecommendRepository : JpaRepository<ReviewRecommend, Int> {
    fun findByReviewAndMember(review: Review, member: Member): Optional<ReviewRecommend>
    fun countByReviewAndIsRecommendedTrue(review: Review): Int
    fun countByReviewAndIsRecommendedFalse(review: Review): Int
}
