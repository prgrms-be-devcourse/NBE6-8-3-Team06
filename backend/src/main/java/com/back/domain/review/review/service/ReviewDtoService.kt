package com.back.domain.review.review.service;

import com.back.domain.book.book.entity.Book;
import com.back.domain.member.member.entity.Member;
import com.back.domain.review.review.dto.ReviewRequestDto;
import com.back.domain.review.review.dto.ReviewResponseDto;
import com.back.domain.review.review.entity.Review;
import com.back.domain.review.reviewRecommend.service.ReviewRecommendService;
import com.back.global.dto.PageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewDtoService {
    private final ReviewRecommendService reviewRecommendService;

    public ReviewResponseDto reviewToReviewResponseDto(Review review, Member member) {
        return ReviewResponseDto.builder()
                .id(review.getId())
                .content(review.getContent())
                .rate(review.getRate())
                .memberName(review.getMember().getName())
                .memberId(review.getMember().getId())
                .likeCount(review.getLikeCount())
                .dislikeCount(review.getDislikeCount())
                .isRecommended(reviewRecommendService.isRecommended(review, member))
                .createdDate(review.getCreateDate())
                .modifiedDate(review.getModifyDate())
                .build();
    }

    public Review reviewRequestDtoToReview(ReviewRequestDto reviewRequestDto, Member member, Book book) {
        return new Review(
                reviewRequestDto.content(),
                reviewRequestDto.rate(),
                member,
                book
        );
    }

    public void updateReviewFromRequest(Review review, ReviewRequestDto reviewRequestDto) {
        review.setContent(reviewRequestDto.content());
        review.setRate(reviewRequestDto.rate());
    }

    public PageResponseDto<ReviewResponseDto> reviewsToReviewResponseDtos(Page<Review> reviewPage, Member member) {
        return new PageResponseDto<>(reviewPage.map((review)-> reviewToReviewResponseDto(review, member)));
    }
}
