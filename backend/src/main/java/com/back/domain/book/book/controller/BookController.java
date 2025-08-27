package com.back.domain.book.book.controller;

import com.back.domain.book.book.dto.BookDetailDto;
import com.back.domain.book.book.dto.BookSearchDto;
import com.back.domain.book.book.service.BookService;
import com.back.domain.member.member.entity.Member;
import com.back.global.dto.PageResponseDto;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "도서 관리")
public class BookController {
    private final BookService bookService;
    private final Rq rq;

    // 검색어 검증
    private String validateAndTrimQuery(String query, String errorCode, String errorMessage) {
        if (query == null || query.trim().isEmpty()) {
            throw new ServiceException(errorCode, errorMessage);
        }
        return query.trim();
    }

    // ISBN 검증 및 정리
    private String validateAndCleanIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new ServiceException("400-7", "ISBN을 입력해주세요.");
        }

        String cleanIsbn = isbn.trim().replaceAll("-", "");
        if (!cleanIsbn.matches("\\d{13}")) {
            throw new ServiceException("400-8", "올바른 ISBN-13 형식이 아닙니다. (13자리 숫자)");
        }

        return cleanIsbn;
    }

    @GetMapping
    @Operation(summary = "전체 책 조회")
    public RsData<PageResponseDto<BookSearchDto>> getAllBooks(
            @PageableDefault(size = 9, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable,
            HttpServletRequest request) {

        Member member = rq.getActor();

        if (member != null) {
            log.debug("로그인된 사용자로 책 조회: {}", member.getEmail());
        } else {
            log.debug("비로그인 사용자로 책 조회");
        }

        Page<BookSearchDto> books = bookService.getAllBooks(pageable, member);
        PageResponseDto<BookSearchDto> pageResponse = new PageResponseDto<>(books);

        return new RsData<>("200-1", "전체 책 조회 성공", pageResponse);
    }

    @GetMapping("/search")
    @Operation(summary = "책 검색")
    public RsData<PageResponseDto<BookSearchDto>> searchBooks(
            @RequestParam String query,
            @PageableDefault(size = 9, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable,
            HttpServletRequest request) {

        String validQuery = validateAndTrimQuery(query, "400-6", "검색어를 입력해주세요.");
        Member member = rq.getActor();

        Page<BookSearchDto> books = bookService.searchBooks(validQuery, pageable, member);
        PageResponseDto<BookSearchDto> pageResponse = new PageResponseDto<>(books);

        if (books.isEmpty()) {
            return new RsData<>("200-2", "검색 결과가 없습니다.", pageResponse);
        }

        return new RsData<>("200-1", books.getTotalElements() + "개의 책을 찾았습니다.", pageResponse);
    }

    @GetMapping("/isbn/{isbn}")
    @Operation(summary = "ISBN으로 책 검색")
    public RsData<BookSearchDto> getBookByIsbn(@PathVariable String isbn, HttpServletRequest request) {
        String cleanIsbn = validateAndCleanIsbn(isbn);
        Member member = rq.getActor();

        BookSearchDto book = bookService.getBookByIsbn(cleanIsbn, member);

        if (book == null) {
            return new RsData<>("404-1", "해당 ISBN의 책을 찾을 수 없습니다.", null);
        }

        return new RsData<>("200-3", "ISBN으로 책 조회 성공", book);
    }

    @GetMapping("/{id}")
    @Operation(summary = "id로 책 검색 (상세 정보 포함))")
    public RsData<BookDetailDto> getBookById(
            @PathVariable int id,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Member member = rq.getActor();
        BookDetailDto bookDetail = bookService.getBookDetailById(id, pageable, member);

        if (bookDetail == null) {
            return new RsData<>("404-1", "해당 ID의 책을 찾을 수 없습니다.", null);
        }

        return new RsData<>("200-4", "책 상세 조회 성공", bookDetail);
    }

    @GetMapping("/categories")
    @Operation(summary = "카테고리별 책 조회")
    public RsData<PageResponseDto<BookSearchDto>> getBooksByCategory(
            @RequestParam String categoryName,
            @PageableDefault(size = 9, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable,
            HttpServletRequest request) {

        String validCategoryName = validateAndTrimQuery(categoryName, "400-9", "카테고리 이름을 입력해주세요.");
        Member member = rq.getActor();

        if (member != null) {
            log.debug("로그인된 사용자로 카테고리별 책 조회: {} - {}", member.getEmail(), validCategoryName);
        } else {
            log.debug("비로그인 사용자로 카테고리별 책 조회: {}", validCategoryName);
        }

        Page<BookSearchDto> books = bookService.getBooksByCategory(validCategoryName, pageable, member);
        PageResponseDto<BookSearchDto> pageResponse = new PageResponseDto<>(books);

        if (books.isEmpty()) {
            return new RsData<>("200-5", "해당 카테고리에 책이 없습니다.", pageResponse);
        }

        return new RsData<>("200-6", validCategoryName + " 카테고리의 " + books.getTotalElements() + "개의 책을 찾았습니다.", pageResponse);
    }

    @GetMapping("/search/category")
    @Operation(summary = "검색어와 카테고리로 책 검색")
    public RsData<PageResponseDto<BookSearchDto>> searchBooksByCategory(
            @RequestParam String query,
            @RequestParam String categoryName,
            @PageableDefault(size = 9, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable) {

        String validQuery = validateAndTrimQuery(query, "400-6", "검색어를 입력해주세요.");
        String validCategoryName = validateAndTrimQuery(categoryName, "400-9", "카테고리 이름을 입력해주세요.");
        Member member = rq.getActor();

        if (member != null) {
            log.debug("로그인된 사용자로 검색어+카테고리 책 조회: {} - query: {}, category: {}",
                    member.getEmail(), validQuery, validCategoryName);
        } else {
            log.debug("비로그인 사용자로 검색어+카테고리 책 조회: query: {}, category: {}", validQuery, validCategoryName);
        }

        Page<BookSearchDto> books = bookService.searchBooksByCategory(validQuery, validCategoryName, pageable, member);
        PageResponseDto<BookSearchDto> pageResponse = new PageResponseDto<>(books);

        if (books.isEmpty()) {
            return new RsData<>("200-7", "'" + validCategoryName + "' 카테고리에서 '" + validQuery + "'에 대한 검색 결과가 없습니다.", pageResponse);
        }

        return new RsData<>("200-8",
                "'" + validCategoryName + "' 카테고리에서 '" + validQuery + "'로 " + books.getTotalElements() + "개의 책을 찾았습니다.",
                pageResponse);
    }
}