package com.back.domain.review.review.service;

import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.service.BookService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.review.review.dto.ReviewRequestDto;
import com.back.domain.review.review.dto.ReviewResponseDto;
import com.back.domain.review.review.entity.Review;
import com.back.domain.review.review.repository.ReviewRepository;
import com.back.global.dto.PageResponseDto;
import com.back.global.exception.ServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewDtoService reviewDtoService;
    private final BookService bookService;

    public Optional<Review> findLatest(){
        return reviewRepository.findFirstByOrderByIdDesc();
    }

    @Transactional
    public void addReview(Book book, Member member, ReviewRequestDto reviewRequestDto){
        Review review = reviewDtoService.reviewRequestDtoToReview(reviewRequestDto, member, book);
        if (reviewRepository.findByBookAndMember(book, member).isPresent()) {
            throw new ServiceException("400-1", "Review already exists");
        }
        reviewRepository.save(review);
        bookService.updateBookAvgRate(book);
    }

    @Transactional
    public void deleteReview(Book book, Member member) {
        Review review = reviewRepository.findByBookAndMember(book, member)
                .orElseThrow(() -> new NoSuchElementException("review not found"));
        reviewRepository.delete(review);
        bookService.updateBookAvgRate(book);
    }

    @Transactional
    public void modifyReview(Book book, Member member, ReviewRequestDto reviewRequestDto) {
        Review review = reviewRepository.findByBookAndMember(book, member)
                .orElseThrow(() -> new NoSuchElementException("review not found"));
        reviewDtoService.updateReviewFromRequest(review, reviewRequestDto);
        reviewRepository.save(review);
        bookService.updateBookAvgRate(book);
    }

    public long count() {
        return reviewRepository.count();
    }

    public Optional<Review> findByBookAndMember(Book book, Member member) {
        return reviewRepository.findByBookAndMember(book, member);
    }

    public Optional<Review> findById(int reviewId) {
        return reviewRepository.findById(reviewId);
    }

    public Page<Review> findByBookOrderByCreateDateDesc(Book book, Pageable pageable){
        return reviewRepository.findByBookOrderByCreateDateDesc(book, pageable);
    }

    public PageResponseDto<ReviewResponseDto> getPageReviewResponseDto(Book book, Pageable pageable, Member member) {
        Page<Review> reviewPage = reviewRepository.findByBookOrderByCreateDateDesc(book, pageable);
        return reviewDtoService.reviewsToReviewResponseDtos(reviewPage, member);
    }
}
