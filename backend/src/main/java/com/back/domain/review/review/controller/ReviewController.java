package com.back.domain.review.review.controller;

import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.repository.BookRepository;

import com.back.domain.member.member.service.MemberService;
import com.back.domain.review.review.dto.ReviewRequestDto;
import com.back.domain.review.review.dto.ReviewResponseDto;
import com.back.domain.review.review.entity.Review;
import com.back.domain.review.review.service.ReviewDtoService;
import com.back.domain.review.review.service.ReviewService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.review.reviewRecommend.service.ReviewRecommendService;
import com.back.global.dto.PageResponseDto;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final ReviewDtoService reviewDtoService;
    private final BookRepository bookRepository;
    private final Rq rq;
    private final ReviewRecommendService reviewRecommendService;

    @GetMapping("/{book_id}")
    public RsData<ReviewResponseDto> getReview(@PathVariable("book_id") int bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new NoSuchElementException("Book not found"));
        Member member = rq.getActor();
        if (member == null) {
            return new RsData<>("401-1", "Unauthorized access");
        }
        Review review = reviewService.findByBookAndMember(book, member).orElseThrow(()->new NoSuchElementException("Review not found"));
        return new RsData<>("200-1", "Review read successfully", reviewDtoService.reviewToReviewResponseDto(review, member));
    }

    @GetMapping("/{book_id}/list")
    public RsData<PageResponseDto<ReviewResponseDto>> getReviews(
            @PathVariable("book_id") int bookId,
            @PageableDefault(size=10, sort="id", direction=Sort.Direction.DESC) Pageable pageable
            ) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new NoSuchElementException("Book not found"));
        Member member = rq.getActor();
        if (member == null) {
            return new RsData<>("401-1", "Unauthorized access");
        }
        PageResponseDto<ReviewResponseDto> reviews = reviewService.getPageReviewResponseDto(book, pageable, member);
        return new RsData<>("200-1", "Reviews fetched successfully", reviews);
    }

    @PostMapping("/{book_id}")
    public RsData<Void> create(@PathVariable("book_id") int bookId, @RequestBody ReviewRequestDto reviewRequestDto) {
        Book book = bookRepository.findById(bookId).orElseThrow(()->new NoSuchElementException("Book not found"));
        Member member = rq.getActor();
        reviewService.addReview(book, member, reviewRequestDto);
        return new RsData<>("201-1", "Reviews fetched successfully");
    }

    @DeleteMapping("/{book_id}")
    public RsData<Void> delete(@PathVariable("book_id") int bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new NoSuchElementException("Book not found"));
        Member member = rq.getActor();
        if (member == null) {
            return new RsData<>("401-1", "Unauthorized access");
        }
        reviewService.deleteReview(book, member);
        return new RsData<>("200-1", "Review deleted successfully");
    }

    @PutMapping("/{book_id}")
    public RsData<Void> modify(@PathVariable("book_id") int bookId, @RequestBody ReviewRequestDto reviewRequestDto) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new NoSuchElementException("Book not found"));
        Member member = rq.getActor();
        if (member == null) {
            return new RsData<>("401-1", "Unauthorized access");
        }
        reviewService.modifyReview(book, member, reviewRequestDto);
        return new RsData<>("200-1", "Review modified successfully");
    }

}
