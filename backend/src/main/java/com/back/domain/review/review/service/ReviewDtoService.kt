package com.back.domain.review.review.service

import com.back.domain.book.book.entity.Book
import com.back.domain.member.member.entity.Member
import com.back.domain.review.review.dto.ReviewRequestDto
import com.back.domain.review.review.dto.ReviewResponseDto
import com.back.domain.review.review.entity.Review
import com.back.domain.review.reviewRecommend.service.ReviewRecommendService
import com.back.domain.review.reviewReport.entity.ReviewReportState
import com.back.global.dto.PageResponseDto
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import java.util.function.Function


@Service
class ReviewDtoService(
    private val reviewRecommendService: ReviewRecommendService
) {


    fun reviewToReviewResponseDto(review: Review, member: Member?): ReviewResponseDto {
    val reviewResponseDto = ReviewResponseDto(
        id = review.id,
        content = review.content,
        rate = review.rate,
        memberName = review.member.getName(),
        memberId = review.member.id,
        likeCount = review.likeCount,
        dislikeCount = review.dislikeCount,
        isRecommended = reviewRecommendService.isRecommended(review, member),
        createdDate = review.createDate,
        modifiedDate = review.modifyDate,
        spoiler = review.spoiler,
        reportState = review.reportState,
        adminMessage = review.adminMessage,
    )
        return reviewResponseDto
    }

    fun reviewRequestDtoToReview(reviewRequestDto: ReviewRequestDto, member: Member, book: Book): Review {
        return Review(
            content =  reviewRequestDto.content,
            rate =  reviewRequestDto.rate,
            spoiler =  reviewRequestDto.spoiler,
            member = member,
            book = book,
        )
    }

    fun updateReviewFromRequest(review: Review, reviewRequestDto: ReviewRequestDto) {
        review.content = reviewRequestDto.content
        review.rate = reviewRequestDto.rate
        if (review.reportState == ReviewReportState.EDIT_REQUIRED){
            review.reportState = ReviewReportState.NOT_REPORTED
            review.adminMessage = null
        }
    }

    fun reviewsToReviewResponseDtos(reviewPage: Page<Review>, member: Member): PageResponseDto<ReviewResponseDto> {
        return PageResponseDto(reviewPage.map(Function { review: Review ->
            reviewToReviewResponseDto(
                review = review,
                member = member
            )
        }))
    }
}
