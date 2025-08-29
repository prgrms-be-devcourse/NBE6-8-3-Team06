package com.back.domain.review.reviewRecommend.controller

import com.back.domain.member.member.entity.Member
import com.back.domain.review.review.service.ReviewService
import com.back.domain.review.reviewRecommend.service.ReviewRecommendService
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import lombok.RequiredArgsConstructor
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/reviewRecommend")
class ReviewRecommendController(
    private val reviewService: ReviewService,
    private val reviewRecommendService: ReviewRecommendService,
    private val rq: Rq
) {

    @PostMapping("/{review_id}/{is_recommend}")
    fun recommendReview(
        @PathVariable("review_id") reviewId: Int,
        @PathVariable("is_recommend") isRecommend: Boolean
    ): RsData<Void> {
        val member: Member? = rq.actor
        if (member == null) {
            return RsData("401-1", "Unauthorized access")
        }
        reviewRecommendService.recommendReview(reviewId, member, isRecommend)
        return RsData("201-1", "Review recommended successfully")
    }

    @PutMapping("/{review_id}/{is_recommend}")
    fun modifyRecommendReview(
        @PathVariable("review_id") reviewId: Int,
        @PathVariable("is_recommend") isRecommend: Boolean
    ): RsData<Void> {
        val member: Member? = rq.actor
        if (member == null) {
            return RsData("401-1", "Unauthorized access")
        }
        reviewRecommendService.modifyRecommendReview(reviewId, member, isRecommend)
        return RsData("200-1", "Review recommendation modified successfully")
    }

    @DeleteMapping("/{review_id}")
    fun cancelRecommendReview(@PathVariable("review_id") reviewId: Int): RsData<Void> {
        val member:Member? = rq.actor
        if (member == null) {
            return RsData("401-1", "Unauthorized access")
        }
        reviewRecommendService.cancelRecommendReview(reviewId, member)
        return RsData("200-1", "Review recommendation cancelled successfully")
    }
}
