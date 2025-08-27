package com.back.domain.review.reviewRecommend.repository;

import com.back.domain.member.member.entity.Member;
import com.back.domain.review.review.entity.Review;
import com.back.domain.review.reviewRecommend.entity.ReviewRecommend;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReviewRecommendRepository extends JpaRepository<ReviewRecommend, Integer> {

    Optional<ReviewRecommend> findByReviewAndMember(Review review, Member member);
    Integer countByReviewAndIsRecommendedTrue(Review review);
    Integer countByReviewAndIsRecommendedFalse(Review review);
}
