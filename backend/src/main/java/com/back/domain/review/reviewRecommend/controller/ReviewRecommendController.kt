package com.back.domain.review.reviewRecommend.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.review.review.entity.Review;
import com.back.domain.review.review.service.ReviewService;
import com.back.domain.review.reviewRecommend.service.ReviewRecommendService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/reviewRecommend")
@RequiredArgsConstructor
public class ReviewRecommendController {
    private final ReviewService reviewService;
    private final ReviewRecommendService reviewRecommendService;
    private final Rq rq;

    @PostMapping("/{review_id}/{is_recommend}")
    public RsData<Void> recommendReview(@PathVariable("review_id") int reviewId, @PathVariable("is_recommend") boolean isRecommend) {
        Member member = rq.getActor();
        if (member == null) {
            return new RsData<>("401-1", "Unauthorized access");
        }
        reviewRecommendService.recommendReview(reviewId, member, isRecommend);
        return new RsData<>("201-1", "Review recommended successfully");
    }

    @PutMapping("/{review_id}/{is_recommend}")
    public RsData<Void> modifyRecommendReview(@PathVariable("review_id") int reviewId, @PathVariable("is_recommend") boolean isRecommend) {
        Member member = rq.getActor();
        if (member == null) {
            return new RsData<>("401-1", "Unauthorized access");
        }
        reviewRecommendService.modifyRecommendReview(reviewId, member, isRecommend);
        return new RsData<>("200-1", "Review recommendation modified successfully");
    }

    @DeleteMapping("/{review_id}")
    public RsData<Void> cancelRecommendReview(@PathVariable("review_id") int reviewId) {
        Member member = rq.getActor();
        if (member == null) {
            return new RsData<>("401-1", "Unauthorized access");
        }
        reviewRecommendService.cancelRecommendReview(reviewId, member);
        return new RsData<>("200-1", "Review recommendation cancelled successfully");
    }
}
