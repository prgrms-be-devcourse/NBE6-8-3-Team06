package com.back.domain.review.review.service

import com.back.domain.book.book.entity.Book
import com.back.domain.book.book.repository.BookRepository
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
    private val reviewRecommendService: ReviewRecommendService,
    private val bookRepository: BookRepository,
) {


    fun findLatest(): Review? {
        return reviewRepository.findFirstByOrderByIdDesc()
    }

    @Transactional
    fun addReview(bookId: Int, member: Member, reviewRequestDto: ReviewRequestDto) {
        val book = bookRepository.findById(bookId)
            .orElseThrow(Supplier { NoSuchElementException("Book not found") })
        val review = reviewDtoService.reviewRequestDtoToReview(reviewRequestDto, member, book)
        reviewRepository.findByBookAndMember(book, member)?.let {
            throw ServiceException("400-1", "Review already exists")
        }
        reviewRepository.save(review)
        bookService.updateBookAvgRate(book)
    }

    @Transactional
    fun deleteReview(bookId: Int, member: Member) {
        val book = bookRepository.findById(bookId)
            .orElseThrow(Supplier { NoSuchElementException("Book not found") })
        val review = reviewRepository.findByBookAndMember(book, member)?: throw NoSuchElementException("review not found")
        review.deleted = true
        bookService.updateBookAvgRate(book)
    }

    @Transactional
    fun modifyReview(bookId: Int, member: Member, reviewRequestDto: ReviewRequestDto) {
        val book = bookRepository.findById(bookId)
            .orElseThrow(Supplier { NoSuchElementException("Book not found") })
        val review = reviewRepository.findByBookAndMember(book, member)?: throw NoSuchElementException("review not found")
        reviewDtoService.updateReviewFromRequest(review, reviewRequestDto)
        reviewRepository.save(review)
        bookService.updateBookAvgRate(book)
    }

    fun count(): Long {
        return reviewRepository.count()
    }

    @Transactional(readOnly = true)
    fun findByBookAndMember(bookId: Int, member: Member): Review? {
        val book = bookRepository.findById(bookId)
            .orElseThrow(Supplier { NoSuchElementException("Book not found") })
        return reviewRepository.findByBookAndMember(book, member)
    }

    fun findById(reviewId: Int): Review? {
        return reviewRepository.findById(reviewId).orElse(null)
    }

    fun findByBookOrderByCreateDateDesc(book: Book, pageable: Pageable): Page<Review> {
        return reviewRepository.findByBookOrderByCreateDateDesc(book, pageable)
    }

    @Transactional(readOnly = true)
    fun getPageReviewResponseDto(bookId: Int, pageable: Pageable, member: Member): PageResponseDto<ReviewResponseDto> {
        val book = bookRepository.findById(bookId)
            .orElseThrow(Supplier { NoSuchElementException("Book not found") })
        val reviewPage: Page<Review> = reviewRepository.findByBookOrderByCreateDateDesc(book, pageable)
        return reviewDtoService.reviewsToReviewResponseDtos(reviewPage, member)
    }

    fun hardDelete(days:Int){
        reviewRepository.hardDeleteByElapsedDays(days = days)
    }
}
