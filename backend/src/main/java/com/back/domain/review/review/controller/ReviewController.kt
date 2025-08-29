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
@RequestMapping("/reviews")
class ReviewController(
    private val reviewService: ReviewService,
    private val reviewDtoService: ReviewDtoService,
    private val bookRepository: BookRepository,
    private val rq: Rq,
) {


    @GetMapping("/{book_id}")
    fun getReview(@PathVariable("book_id") bookId: Int): RsData<ReviewResponseDto> {
        val book = bookRepository.findById(bookId)
            .orElseThrow(Supplier { NoSuchElementException("Book not found") })
        val member: Member = rq.getAuthenticatedActor()
        val review = reviewService.findByBookAndMember(book, member)
            .orElseThrow(Supplier { NoSuchElementException("Review not found") })
        return RsData(
            "200-1",
            "Review read successfully",
            reviewDtoService.reviewToReviewResponseDto(review, member)
        )
    }

    @GetMapping("/{book_id}/list")
    fun getReviews(
        @PathVariable("book_id") bookId: Int,
        @PageableDefault(size = 10, sort = ["id"], direction = Sort.Direction.DESC) pageable: Pageable
    ): RsData<PageResponseDto<ReviewResponseDto>> {
        val book = bookRepository.findById(bookId)
            .orElseThrow(Supplier { NoSuchElementException("Book not found") })
        val member: Member = rq.getAuthenticatedActor()
        val reviews = reviewService.getPageReviewResponseDto(book, pageable, member)
        return RsData("200-1", "Reviews fetched successfully", reviews)
    }

    @PostMapping("/{book_id}")
    fun create(
        @PathVariable("book_id") bookId: Int,
        @RequestBody reviewRequestDto: ReviewRequestDto
    ): RsData<Void> {
        val book = bookRepository.findById(bookId)
            .orElseThrow(Supplier { NoSuchElementException("Book not found") })
        val member = rq.getAuthenticatedActor()
        reviewService.addReview(book, member, reviewRequestDto)
        return RsData("201-1", "Reviews fetched successfully")
    }

    @DeleteMapping("/{book_id}")
    fun delete(
        @PathVariable("book_id") bookId: Int
    ): RsData<Void> {
        val book = bookRepository.findById(bookId)
            .orElseThrow(Supplier { NoSuchElementException("Book not found") })
        val member: Member = rq.getAuthenticatedActor()
        reviewService.deleteReview(book, member)
        return RsData("200-1", "Review deleted successfully")
    }

    @PutMapping("/{book_id}")
    fun modify(
        @PathVariable("book_id") bookId: Int,
        @RequestBody reviewRequestDto: ReviewRequestDto
    ): RsData<Void> {
        val book = bookRepository.findById(bookId)
            .orElseThrow(Supplier { NoSuchElementException("Book not found") })
        val member: Member = rq.getAuthenticatedActor()
        reviewService.modifyReview(book, member, reviewRequestDto)
        return RsData("200-1", "Review modified successfully")
    }
}
