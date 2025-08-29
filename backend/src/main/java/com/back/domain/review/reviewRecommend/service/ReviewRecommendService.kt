package com.back.domain.review.reviewRecommend.service

import com.back.domain.member.member.entity.Member
import com.back.domain.review.review.entity.Review
import com.back.domain.review.review.repository.ReviewRepository
import com.back.domain.review.reviewRecommend.entity.ReviewRecommend
import com.back.domain.review.reviewRecommend.repository.ReviewRecommendRepository
import com.back.global.exception.ServiceException
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Supplier

@Service
class ReviewRecommendService(
    private val reviewRepository: ReviewRepository,
    private val reviewRecommendRepository: ReviewRecommendRepository
) {

    @Transactional
    fun recommendReview(reviewId: Int, member: Member, isRecommend: Boolean) {
        val review: Review = reviewRepository.findWithLockById(reviewId)
            .orElseThrow(Supplier { NoSuchElementException("Review not found") })
        val reviewRecommend = ReviewRecommend(review, member, isRecommend)
        if (reviewRecommendRepository.findByReviewAndMember(review, member).isPresent()) {
            throw ServiceException("400-1", "Review recommendation already exists")
        }
        reviewRecommendRepository.save<ReviewRecommend?>(reviewRecommend)
        if (isRecommend) {
            review.incLike()
        } else {
            review.incDislike()
        }
        reviewRepository.save(review)
    }

    @Transactional
    fun modifyRecommendReview(reviewId: Int, member: Member, isRecommend: Boolean) {
        val review: Review = reviewRepository.findWithLockById(reviewId)
            .orElseThrow(Supplier { NoSuchElementException("Review not found") })
        val reviewRecommend = reviewRecommendRepository.findByReviewAndMember(review, member)
            .orElseThrow(Supplier { NoSuchElementException("Review recommendation not found") })
        if (reviewRecommend.isRecommended == isRecommend) {
            throw ServiceException("400-2", "Review recommendation already set to this value")
        }
        reviewRecommend.isRecommended = isRecommend
        reviewRecommendRepository.save(reviewRecommend)
        if (isRecommend) {
            review.incLike()
            review.decDislike()
        } else {
            review.decLike()
            review.incDislike()
        }
    }

    @Transactional
    fun cancelRecommendReview(reviewId: Int, member: Member) {
        val review: Review = reviewRepository.findWithLockById(reviewId)
            .orElseThrow(Supplier { NoSuchElementException("Review not found") })
        val reviewRecommend = reviewRecommendRepository!!.findByReviewAndMember(review, member)
            .orElseThrow(Supplier { NoSuchElementException("Review recommendation not found") })
        reviewRecommendRepository.delete(reviewRecommend)
        if (reviewRecommend.isRecommended) {
            review.decLike()
        } else {
            review.decDislike()
        }
    }

    fun isRecommended(review: Review, member: Member?): Boolean? {
        if (member == null) return null
        return reviewRecommendRepository.findByReviewAndMember(review, member)
            .map(ReviewRecommend::isRecommended)
            .orElse(null)
    }
}
