package com.back.domain.book.book.controller

import com.back.domain.book.book.dto.BookDetailDto
import com.back.domain.book.book.dto.BookSearchDto
import com.back.domain.book.book.service.BookService
import com.back.global.dto.PageResponseDto
import com.back.global.exception.ServiceException
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/books")
@Tag(name = "도서 관리")
class BookController(
    private val bookService: BookService,
    private val rq: Rq
) {

    companion object {
        private val logger = LoggerFactory.getLogger(BookController::class.java)
    }

    // 검색어 검증 - extension function 스타일로 변경
    private fun String?.validateAndTrim(errorCode: String, errorMessage: String): String =
        this?.trim()?.takeIf { it.isNotEmpty() } ?: throw ServiceException(errorCode, errorMessage)

    // ISBN 검증 및 정리 - 개선된 버전
    private fun String?.validateAndCleanIsbn(): String {
        val trimmed = this?.trim()
        require(!trimmed.isNullOrEmpty()) { throw ServiceException("400-7", "ISBN을 입력해주세요.") }

        val cleanIsbn = trimmed.replace("-", "")
        require(cleanIsbn.matches(Regex("\\d{13}"))) {
            throw ServiceException("400-8", "올바른 ISBN-13 형식이 아닙니다. (13자리 숫자)")
        }

        return cleanIsbn
    }

    @GetMapping
    @Operation(summary = "전체 책 조회")
    fun getAllBooks(
        @PageableDefault(size = 9, sort = ["id"], direction = Sort.Direction.DESC)
        pageable: Pageable,
        request: HttpServletRequest? = null
    ): RsData<PageResponseDto<BookSearchDto>> {
        val member = rq.getActor()

        member?.let {
            logger.debug("로그인된 사용자로 책 조회: ${it.getEmail()}")
        } ?: run {
            logger.debug("비로그인 사용자로 책 조회")
        }

        val books = bookService.getAllBooks(pageable, member)
        val pageResponse = PageResponseDto(books)

        return RsData("200-1", "전체 책 조회 성공", pageResponse)
    }

    @GetMapping("/search")
    @Operation(summary = "책 검색")
    fun searchBooks(
        @RequestParam query: String,
        @PageableDefault(size = 9, sort = ["id"], direction = Sort.Direction.DESC)
        pageable: Pageable,
        request: HttpServletRequest? = null
    ): RsData<PageResponseDto<BookSearchDto>> {
        val validQuery = query.validateAndTrim("400-6", "검색어를 입력해주세요.")
        val member = rq.getActor()

        val books = bookService.searchBooks(validQuery, pageable, member)
        val pageResponse = PageResponseDto(books)

        return if (!books.hasContent()) {
            RsData("200-2", "검색 결과가 없습니다.", pageResponse)
        } else {
            RsData("200-1", "${books.totalElements}개의 책을 찾았습니다.", pageResponse)
        }
    }

    @GetMapping("/isbn/{isbn}")
    @Operation(summary = "ISBN으로 책 검색")
    fun getBookByIsbn(
        @PathVariable isbn: String,
        request: HttpServletRequest? = null
    ): RsData<BookSearchDto?> {
        val cleanIsbn = isbn.validateAndCleanIsbn()
        val member = rq.getActor()

        val book = bookService.getBookByIsbn(cleanIsbn, member)

        return book?.let {
            RsData("200-3", "ISBN으로 책 조회 성공", it)
        } ?: RsData("404-1", "해당 ISBN의 책을 찾을 수 없습니다.", null)
    }

    @GetMapping("/{id}")
    @Operation(summary = "id로 책 검색 (상세 정보 포함)")
    fun getBookById(
        @PathVariable id: Int,
        @PageableDefault(size = 10, sort = ["id"], direction = Sort.Direction.DESC)
        pageable: Pageable
    ): RsData<BookDetailDto?> {
        val member = rq.getActor()
        val bookDetail = bookService.getBookDetailById(id, pageable, member)

        return bookDetail?.let {
            RsData("200-4", "책 상세 조회 성공", it)
        } ?: RsData("404-1", "해당 ID의 책을 찾을 수 없습니다.", null)
    }

    @GetMapping("/categories")
    @Operation(summary = "카테고리별 책 조회")
    fun getBooksByCategory(
        @RequestParam categoryName: String,
        @PageableDefault(size = 9, sort = ["id"], direction = Sort.Direction.DESC)
        pageable: Pageable,
        request: HttpServletRequest? = null
    ): RsData<PageResponseDto<BookSearchDto>> {
        val validCategoryName = categoryName.validateAndTrim("400-9", "카테고리 이름을 입력해주세요.")
        val member = rq.getActor()

        member?.let {
            logger.debug("로그인된 사용자로 카테고리별 책 조회: ${it.getEmail()} - $validCategoryName")
        } ?: run {
            logger.debug("비로그인 사용자로 카테고리별 책 조회: $validCategoryName")
        }

        val books = bookService.getBooksByCategory(validCategoryName, pageable, member)
        val pageResponse = PageResponseDto(books)

        return if (!books.hasContent()) {
            RsData("200-5", "해당 카테고리에 책이 없습니다.", pageResponse)
        } else {
            RsData("200-6", "$validCategoryName 카테고리의 ${books.totalElements}개의 책을 찾았습니다.", pageResponse)
        }
    }

    @GetMapping("/search/category")
    @Operation(summary = "검색어와 카테고리로 책 검색")
    fun searchBooksByCategory(
        @RequestParam query: String,
        @RequestParam categoryName: String,
        @PageableDefault(size = 9, sort = ["id"], direction = Sort.Direction.DESC)
        pageable: Pageable
    ): RsData<PageResponseDto<BookSearchDto>> {
        val validQuery = query.validateAndTrim("400-6", "검색어를 입력해주세요.")
        val validCategoryName = categoryName.validateAndTrim("400-9", "카테고리 이름을 입력해주세요.")
        val member = rq.getActor()

        member?.let {
            logger.debug(
                "로그인된 사용자로 검색어+카테고리 책 조회: ${it.getEmail()} - query: $validQuery, category: $validCategoryName"
            )
        } ?: run {
            logger.debug("비로그인 사용자로 검색어+카테고리 책 조회: query: $validQuery, category: $validCategoryName")
        }

        val books = bookService.searchBooksByCategory(validQuery, validCategoryName, pageable, member)
        val pageResponse = PageResponseDto(books)

        return if (!books.hasContent()) {
            RsData(
                "200-7",
                "'$validCategoryName' 카테고리에서 '$validQuery'에 대한 검색 결과가 없습니다.",
                pageResponse
            )
        } else {
            RsData(
                "200-8",
                "'$validCategoryName' 카테고리에서 '$validQuery'로 ${books.totalElements}개의 책을 찾았습니다.",
                pageResponse
            )
        }
    }
}