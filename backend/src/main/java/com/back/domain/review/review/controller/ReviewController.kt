package com.back.domain.review.review.controller

import com.back.domain.book.book.repository.BookRepository
import com.back.domain.member.member.entity.Member
import com.back.domain.review.review.dto.ReviewRequestDto
import com.back.domain.review.review.dto.ReviewResponseDto
import com.back.domain.review.review.service.ReviewDtoService
import com.back.domain.review.review.service.ReviewService
import com.back.global.dto.PageResponseDto
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import java.util.function.Supplier

@RestController
@RequestMapping("/reviews/{book_id}")
class ReviewController(
    private val reviewService: ReviewService,
    private val reviewDtoService: ReviewDtoService,
    private val bookRepository: BookRepository,
    private val rq: Rq,
) {


    @GetMapping
    fun getReview(@PathVariable("book_id") bookId: Int): RsData<ReviewResponseDto> {
        val member: Member = rq.getAuthenticatedActor()
        val review = reviewService.findByBookAndMember(bookId, member)?: throw NoSuchElementException("Review not found")
        return RsData(
            "200-1",
            "Review read successfully",
            reviewDtoService.reviewToReviewResponseDto(review, member)
        )
    }

    @GetMapping("/list")
    fun getReviews(
        @PathVariable("book_id") bookId: Int,
        @PageableDefault(size = 10, sort = ["id"], direction = Sort.Direction.DESC) pageable: Pageable
    ): RsData<PageResponseDto<ReviewResponseDto>> {
        val member: Member = rq.getAuthenticatedActor()
        val reviews = reviewService.getPageReviewResponseDto(bookId, pageable, member)
        return RsData("200-1", "Reviews fetched successfully", reviews)
    }

    @PostMapping
    fun create(
        @PathVariable("book_id") bookId: Int,
        @RequestBody reviewRequestDto: ReviewRequestDto
    ): RsData<Void> {
        val member = rq.getAuthenticatedActor()
        reviewService.addReview(bookId, member, reviewRequestDto)
        return RsData("201-1", "Reviews fetched successfully")
    }

    @DeleteMapping
    fun delete(
        @PathVariable("book_id") bookId: Int
    ): RsData<Void> {
        val member: Member = rq.getAuthenticatedActor()
        reviewService.deleteReview(bookId, member)
        return RsData("200-1", "Review deleted successfully")
    }

    @PutMapping
    fun modify(
        @PathVariable("book_id") bookId: Int,
        @RequestBody reviewRequestDto: ReviewRequestDto
    ): RsData<Void> {
        val member: Member = rq.getAuthenticatedActor()
        reviewService.modifyReview(bookId, member, reviewRequestDto)
        return RsData("200-1", "Review modified successfully")
    }
}
