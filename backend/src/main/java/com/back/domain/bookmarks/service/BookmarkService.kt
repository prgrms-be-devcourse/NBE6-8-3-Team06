package com.back.domain.bookmarks.service;

import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.bookmarks.constant.ReadState;
import com.back.domain.bookmarks.dto.*;
import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.bookmarks.repository.BookmarkRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.review.review.entity.Review;
import com.back.domain.review.review.repository.ReviewRepository;
import com.back.domain.review.review.service.ReviewService;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import com.back.domain.book.book.entity.Book;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final BookRepository bookRepository;
    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;

    /**
     *  책을 북마크에 추가
     * @param bookId
     * @param member
     * @return Bookmark Entity
     * @throws NoSuchElementException 책이 없는 경우
     * @throws IllegalStateException 이미 추가된 책인 경우
     */
    public Bookmark save(int bookId, Member member) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new NoSuchElementException("%d번 등록된 책이 없습니다.".formatted(bookId)));
        if(bookmarkRepository.existsByMemberAndBook(member, book)) {
            throw new IllegalStateException("이미 추가된 책입니다.");
        }
        Bookmark bookmark = new Bookmark(book, member);
        return bookmarkRepository.save(bookmark);
    }

    /**
     * 사용자의 모든 북마크 목록 조회
     * @param member
     * @return BookmarkDto 리스트
     */
    public List<BookmarkDto> toList(Member member){
        List<Bookmark> bookmarks = bookmarkRepository.findByMember(member);
        //리뷰 포함 리스트 반환
        return convertToBookmarkDtoList(bookmarks, member);
    }

    /**
     * 사용자의 북마크 목록을 페이징, 필터링하여 조회
     * @param member
     * @param page
     * @param size
     * @param sort : type,order
     * @param category
     * @param state
     * @param keyword
     * @return BookmarkDto 페이징
     */
    public Page<BookmarkDto> toPage(Member member, int page, int size, String sort, String category, String state, String keyword) {
        String[] sorts = sort.split(",",2);
        Pageable pageable = PageRequest.of(page, size, sorts[1].equals("desc") ? Sort.by(sorts[0]).descending() : Sort.by(sorts[0]).ascending());

        Page<Bookmark> bookmarks = bookmarkRepository.search(member, category, state, keyword, pageable);
        //리뷰 포함 리스트 반환
        List<BookmarkDto> dtoList = convertToBookmarkDtoList(bookmarks.getContent(), member);
        return new PageImpl<>(dtoList, pageable, bookmarks.getTotalElements());
    }

    /**
     *  북마크 상세 조회
     * @param member
     * @param bookmarkId
     * @return BookmarkDetailDto
     */
    public BookmarkDetailDto getBookmarkById(Member member, int bookmarkId) {
        Bookmark bookmark = bookmarkRepository.findByIdAndMember(bookmarkId, member).orElseThrow(() -> new NoSuchElementException("%d번 데이터가 없습니다.".formatted(bookmarkId)));
        Review review = getReview(bookmark);
        return new BookmarkDetailDto(bookmark, review);
    }

    public Bookmark findById(int bookmarkId) {
        return bookmarkRepository.findById(bookmarkId).orElseThrow(() -> new NoSuchElementException("%d번 데이터가 없습니다.".formatted(bookmarkId)));
    }

    /**
     * 북마크 수정
     * @param member
     * @param bookmarkId
     * @param state
     * @param startReadDate
     * @param endReadDate
     * @param readPage
     * @return BookmarkModifyResponseDto
     */
    public BookmarkModifyResponseDto modifyBookmark(Member member, int bookmarkId, String state, LocalDateTime startReadDate, LocalDateTime endReadDate, int readPage) {
        Bookmark bookmark = findByIdAndMember(bookmarkId, member);
        if(endReadDate != null){
            bookmark.updateEndReadDate(endReadDate);
        }
        if(startReadDate != null){
            bookmark.updateStartReadDate(startReadDate);
        }
        if(readPage > 0){
            bookmark.updateReadPage(readPage);
        }
        if(state != null){
            ReadState readState = ReadState.valueOf(state.toUpperCase());
            bookmark.updateReadState(readState);
        }
        bookmarkRepository.flush();
        return new BookmarkModifyResponseDto(bookmark);
    }

    /**
     * 북마크 삭제
     * @param member
     * @param bookmarkId
     */
    public void deleteBookmark(Member member, int bookmarkId) {
        Bookmark bookmark = findByIdAndMember(bookmarkId, member);
        bookmarkRepository.delete(bookmark);
        //리뷰가 있는 경우, 리뷰 삭제
        if(getReview(bookmark) != null) {
            reviewService.deleteReview(bookmark.getBook(), member);
        }
    }

    //북마크 권한 체크
    private Bookmark findByIdAndMember(int bookmarkId, Member member) {
        return bookmarkRepository.findByIdAndMember(bookmarkId, member).orElseThrow(() -> new ServiceException("403-1", "해당 북마크에 대한 권한이 없습니다."));
    }

    /**
     * 북마크 통계 조회 - 필터 조건
     * @param member
     * @param category
     * @param readState
     * @param keyword
     * @return BookmarkReadStatesDto
     */
    public BookmarkReadStatesDto getReadStatesCount(Member member, String category, String readState, String keyword) {
        ReadStateCount readStateCount = bookmarkRepository.countReadState(member, category, readState, keyword);
        long totalCount = readStateCount.READ()+readStateCount.READING()+readStateCount.WISH();
        double avgRate = 0.0;
        //필터 조건이 있는 경우 리뷰 평점 조회하지 않음. 리뷰 조건은 전체 통계에서만 조회
        if(category == null && readState == null && keyword == null){
            avgRate = getAvgRate(member);
        }
        return new BookmarkReadStatesDto(
               totalCount , avgRate, readStateCount
        );
    }

    private double getAvgRate(Member member) {
        return reviewRepository.findAverageRatingByMember(member).orElse(0.0);
    }

    private Review getReview(Bookmark bookmark) {
        return reviewService.findByBookAndMember(bookmark.getBook(), bookmark.getMember()).orElse(null);
    }

    private ReadState getReadStateByMemberAndBook(Member member, Book book) {
        Optional<Bookmark> bookmark = bookmarkRepository.findByMemberAndBookWithFresh(member, book);
        return bookmark.map(Bookmark::getReadState).orElse(null);
    }

    public ReadState getReadStateByMemberAndBookId(Member member, int bookId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) {
            return null;
        }
        return getReadStateByMemberAndBook(member, book);
    }

    public Map<Integer, ReadState> getReadStatesForBooks(Member member, List<Integer> bookIds) {
        List<Bookmark> bookmarks = bookmarkRepository.findByMember(member);

        return bookmarks.stream()
                .filter(bookmark -> bookIds.contains(bookmark.getBook().getId()))
                .collect(Collectors.toMap(
                        bookmark -> bookmark.getBook().getId(),
                        Bookmark::getReadState
                ));
    }

    /**
     * 북마크 리뷰 조회
     * @param bookmarks
     * @param member
     * @return 북마크 리스트
     */
    private List<BookmarkDto> convertToBookmarkDtoList(List<Bookmark> bookmarks, Member member) {
        if(bookmarks == null || bookmarks.isEmpty()) {
            return Collections.emptyList();
        }
        //사용자가 쓴 리뷰 전체 조회
        List<Review> reviews = reviewRepository.findAllByMember(member);
        //Key : bookId, Value: review
        Map<Integer, Review> reviewMap = reviews.stream().collect(Collectors.toMap(review -> review.getBook().getId(), review -> review));

        return bookmarks.stream().map(bookmark -> {
            Review review = reviewMap.get(bookmark.getBook().getId());
            return new BookmarkDto(bookmark, review);
        }).toList();
    }

    public Bookmark getLatestBookmark(Member member) {
        return bookmarkRepository.getBookmarkByMemberOrderByIdDesc(member).orElseThrow(() -> new NoSuchElementException("조회가능한 북마크가 없습니다."));
    }
}
