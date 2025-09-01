package com.back.domain.review.review.service

import com.back.domain.book.book.entity.Book
import com.back.domain.book.book.service.BookService
import com.back.domain.member.member.entity.Member
import com.back.domain.review.review.dto.ReviewRequestDto
import com.back.domain.review.review.dto.ReviewResponseDto
import com.back.domain.review.review.entity.Review
import com.back.domain.review.review.repository.ReviewRepository
import com.back.domain.review.reviewRecommend.service.ReviewRecommendService
import com.back.global.dto.PageResponseDto
import com.back.global.exception.ServiceException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.function.Supplier

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val reviewDtoService: ReviewDtoService,
    private val bookService: BookService,
    private val reviewRecommendService: ReviewRecommendService
) {


    fun findLatest(): Optional<Review> {
        return reviewRepository.findFirstByOrderByIdDesc()
    }

    @Transactional
    fun addReview(book: Book, member: Member, reviewRequestDto: ReviewRequestDto) {
        val review = reviewDtoService.reviewRequestDtoToReview(reviewRequestDto, member, book)
        if (reviewRepository.findByBookAndMember(book, member).isPresent()) {
            throw ServiceException("400-1", "Review already exists")
        }
        reviewRepository.save(review)
        bookService.updateBookAvgRate(book)
    }

    @Transactional
    fun deleteReview(book: Book, member: Member) {
        val review = reviewRepository.findByBookAndMember(book, member)
            .orElseThrow(Supplier { NoSuchElementException("review not found") })
        reviewRepository.delete(review)
        bookService.updateBookAvgRate(book)
    }

    @Transactional
    fun modifyReview(book: Book, member: Member, reviewRequestDto: ReviewRequestDto) {
        val review = reviewRepository.findByBookAndMember(book, member)
            .orElseThrow(Supplier { NoSuchElementException("review not found") })
        reviewDtoService.updateReviewFromRequest(review, reviewRequestDto)
        reviewRepository.save(review)
        bookService.updateBookAvgRate(book)
    }

    fun count(): Long {
        return reviewRepository.count()
    }

    fun findByBookAndMember(book: Book, member: Member): Optional<Review> {
        return reviewRepository.findByBookAndMember(book, member)
    }

    fun findById(reviewId: Int): Optional<Review?> {
        return reviewRepository.findById(reviewId)
    }

    fun findByBookOrderByCreateDateDesc(book: Book, pageable: Pageable): Page<Review> {
        return reviewRepository.findByBookOrderByCreateDateDesc(book, pageable)
    }

    fun getPageReviewResponseDto(book: Book, pageable: Pageable, member: Member): PageResponseDto<ReviewResponseDto> {
        val reviewPage: Page<Review> = reviewRepository.findByBookOrderByCreateDateDesc(book, pageable)
        return reviewDtoService.reviewsToReviewResponseDtos(reviewPage, member)
    }
}
