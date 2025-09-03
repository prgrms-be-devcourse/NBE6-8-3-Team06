package com.back.domain.book.book.service

import com.back.domain.book.author.entity.Author
import com.back.domain.book.author.repository.AuthorRepository
import com.back.domain.book.book.dto.BookDetailDto
import com.back.domain.book.book.dto.BookSearchDto
import com.back.domain.book.book.entity.Book
import com.back.domain.book.book.repository.BookRepository
import com.back.domain.book.category.entity.Category
import com.back.domain.book.category.repository.CategoryRepository
import com.back.domain.book.client.aladin.AladinApiClient
import com.back.domain.book.client.aladin.dto.AladinBookDto
import com.back.domain.book.wrote.entity.Wrote
import com.back.domain.book.wrote.repository.WroteRepository
import com.back.domain.bookmarks.constant.ReadState
import com.back.domain.bookmarks.repository.BookmarkRepository
import com.back.domain.member.member.entity.Member
import com.back.domain.review.review.repository.ReviewRepository
import com.back.domain.review.review.service.ReviewDtoService
import com.back.global.dto.PageResponseDto
import com.back.global.exception.ServiceException
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// 카테고리 매핑을 위한 데이터 클래스
data class CategoryMapping(
    val categoryName: String,
    val aladinCategoryId: Int,
    val searchTarget: AladinApiClient.SearchTarget = AladinApiClient.SearchTarget.BOOK
)

@Service
class BookService(
    private val bookRepository: BookRepository,
    private val categoryRepository: CategoryRepository,
    private val authorRepository: AuthorRepository,
    private val wroteRepository: WroteRepository,
    private val aladinApiClient: AladinApiClient,
    private val bookmarkRepository: BookmarkRepository,
    private val reviewRepository: ReviewRepository,
    private val reviewDtoService: ReviewDtoService,
) {

    companion object {
        private val log = LoggerFactory.getLogger(BookService::class.java)

        // 카테고리명과 알라딘 카테고리 ID 매핑
        private val CATEGORY_MAPPINGS = mapOf(
            // 국내도서 주요 카테고리
            "소설/시/희곡" to CategoryMapping("소설/시/희곡", 1, AladinApiClient.SearchTarget.BOOK),
            "경제경영" to CategoryMapping("경제경영", 170, AladinApiClient.SearchTarget.BOOK),
            "자기계발" to CategoryMapping("자기계발", 336, AladinApiClient.SearchTarget.BOOK),
            "컴퓨터/모바일" to CategoryMapping("컴퓨터/모바일", 351, AladinApiClient.SearchTarget.BOOK),
            "인문학" to CategoryMapping("인문학", 656, AladinApiClient.SearchTarget.BOOK),
            "역사" to CategoryMapping("역사", 74, AladinApiClient.SearchTarget.BOOK),
            "과학" to CategoryMapping("과학", 987, AladinApiClient.SearchTarget.BOOK),
            "예술/대중문화" to CategoryMapping("예술/대중문화", 517, AladinApiClient.SearchTarget.BOOK),
            "사회과학" to CategoryMapping("사회과학", 798, AladinApiClient.SearchTarget.BOOK),
            "어린이" to CategoryMapping("어린이", 1108, AladinApiClient.SearchTarget.BOOK),
            "청소년" to CategoryMapping("청소년", 1137, AladinApiClient.SearchTarget.BOOK),
            "가정/요리/뷰티" to CategoryMapping("가정/요리/뷰티", 1230, AladinApiClient.SearchTarget.BOOK),
            "건강/취미/레저" to CategoryMapping("건강/취미/레저", 55890, AladinApiClient.SearchTarget.BOOK),
            "여행" to CategoryMapping("여행", 1196, AladinApiClient.SearchTarget.BOOK),
            "종교/역학" to CategoryMapping("종교/역학", 1237, AladinApiClient.SearchTarget.BOOK),
            "외국어" to CategoryMapping("외국어", 1322, AladinApiClient.SearchTarget.BOOK),
            "수험서/자격증" to CategoryMapping("수험서/자격증", 1383, AladinApiClient.SearchTarget.BOOK),
            "만화" to CategoryMapping("만화", 2551, AladinApiClient.SearchTarget.BOOK),
            "에세이" to CategoryMapping("에세이", 55889, AladinApiClient.SearchTarget.BOOK),

            // 외국도서 주요 카테고리
            "외국소설" to CategoryMapping("외국소설", 90842, AladinApiClient.SearchTarget.FOREIGN),
            "외국경영" to CategoryMapping("외국경영", 90835, AladinApiClient.SearchTarget.FOREIGN),
            "외국자기계발" to CategoryMapping("외국자기계발", 90854, AladinApiClient.SearchTarget.FOREIGN),
            "외국컴퓨터" to CategoryMapping("외국컴퓨터", 90859, AladinApiClient.SearchTarget.FOREIGN),
            "외국인문사회" to CategoryMapping("외국인문사회", 90853, AladinApiClient.SearchTarget.FOREIGN),
            "외국자연과학" to CategoryMapping("외국자연과학", 90855, AladinApiClient.SearchTarget.FOREIGN),
            "외국의학" to CategoryMapping("외국의학", 90852, AladinApiClient.SearchTarget.FOREIGN),
            "외국어학" to CategoryMapping("외국어학", 90861, AladinApiClient.SearchTarget.FOREIGN),
            "외국어린이" to CategoryMapping("외국어린이", 106165, AladinApiClient.SearchTarget.FOREIGN)
        )

        // API에서 가져올 최소 책 수
        private const val MIN_BOOKS_TO_FETCH = 50
        // API에서 가져올 최대 책 수
        private const val MAX_BOOKS_TO_FETCH = 100
    }

    /**
     * 카테고리별 책 조회 (개선된 버전) - DB에 없으면 알라딘 API에서 가져옴
     */
    @Transactional
    fun getBooksByCategory(categoryName: String, pageable: Pageable, member: Member? = null): Page<BookSearchDto> {
        log.info(
            "카테고리별 유효한 책 조회: category={}, page={}, size={}, sort={}, member={}",
            categoryName, pageable.pageNumber, pageable.pageSize, pageable.sort,
            member?.id ?: "null"
        )

        try {
            // 1. DB에서 해당 카테고리의 유효한 책들 조회
            val validBookPage = bookRepository.findValidBooksByCategory(categoryName, pageable)

            // 2. DB에 충분한 책이 있으면 바로 반환
            if (validBookPage.hasContent() && validBookPage.totalElements >= MIN_BOOKS_TO_FETCH) {
                log.info("DB에서 충분한 책을 찾음: {}권", validBookPage.totalElements)
                return validBookPage.map { convertToDto(it, member) }
            }

            // 3. DB에 책이 부족하면 알라딘 API에서 가져와서 보충
            log.info("DB에 책이 부족하여 알라딘 API에서 보충: 현재 {}권", validBookPage.totalElements)

            val categoryMapping = CATEGORY_MAPPINGS[categoryName]
            if (categoryMapping != null) {
                fetchAndSaveBooksFromApi(categoryMapping)

                // 4. API 데이터 저장 후 다시 DB에서 조회
                val updatedBookPage = bookRepository.findValidBooksByCategory(categoryName, pageable)
                log.info("API 데이터 저장 후 재조회: {}권", updatedBookPage.totalElements)

                return updatedBookPage.map { convertToDto(it, member) }
            } else {
                // 매핑되지 않은 카테고리는 DB 결과만 반환
                log.warn("매핑되지 않은 카테고리: {}", categoryName)
                return validBookPage.map { convertToDto(it, member) }
            }

        } catch (e: Exception) {
            log.error("카테고리별 책 조회 중 오류 발생: category={}, error={}", categoryName, e.message)
            throw ServiceException("500-3", "카테고리별 책 조회 중 오류가 발생했습니다.")
        }
    }

    /**
     * 검색어와 카테고리로 책 검색 (개선된 버전)
     */
    @Transactional
    fun searchBooksByCategory(
        query: String,
        categoryName: String,
        pageable: Pageable,
        member: Member? = null
    ): Page<BookSearchDto> {
        log.info(
            "검색어+카테고리로 유효한 책 조회: query={}, category={}, page={}, size={}, sort={}, member={}",
            query, categoryName, pageable.pageNumber, pageable.pageSize, pageable.sort,
            member?.id ?: "null"
        )

        try {
            // 1. DB에서 검색어+카테고리로 조회
            val validBookPage = bookRepository.findValidBooksByQueryAndCategory(query, categoryName, pageable)

            // 2. 결과가 있으면 반환
            if (validBookPage.hasContent()) {
                log.info("DB 검색 결과: {}권", validBookPage.totalElements)
                return validBookPage.map { convertToDto(it, member) }
            }

            // 3. DB에 결과가 없으면 일반 검색으로 폴백하되, 해당 카테고리만 필터링
            log.info("DB에 검색 결과가 없어서 일반 검색 후 카테고리 필터링 수행")
            val generalSearchResults = searchBooks(query, MAX_BOOKS_TO_FETCH, member)

            // 카테고리 필터링
            val filteredResults = generalSearchResults.filter { it.categoryName == categoryName }

            // 페이징 처리 (수동으로 구현)
            val startIndex = pageable.pageNumber * pageable.pageSize
            val endIndex = minOf(startIndex + pageable.pageSize, filteredResults.size)

            if (startIndex < filteredResults.size) {
                val pagedResults = filteredResults.subList(startIndex, endIndex)
                log.info("카테고리 필터링 후 결과: {}권 (전체 {}권 중 {}~{})",
                    pagedResults.size, filteredResults.size, startIndex, endIndex)

                return org.springframework.data.domain.PageImpl(
                    pagedResults,
                    pageable,
                    filteredResults.size.toLong()
                )
            } else {
                return Page.empty(pageable)
            }

        } catch (e: Exception) {
            log.error(
                "검색어+카테고리 책 조회 중 오류 발생: query={}, category={}, error={}",
                query, categoryName, e.message
            )
            throw ServiceException("500-4", "검색어+카테고리 책 조회 중 오류가 발생했습니다.")
        }
    }

    /**
     * 알라딘 API에서 카테고리별 책 데이터를 가져와서 DB에 저장
     */
    private fun fetchAndSaveBooksFromApi(categoryMapping: CategoryMapping) {
        try {
            log.info("알라딘 API에서 {} 카테고리 데이터 가져오기 시작 (ID: {})",
                categoryMapping.categoryName, categoryMapping.aladinCategoryId)

            // 알라딘 API에서 카테고리별 책 데이터 조회 (단순히 신간 전체로 조회)
            val apiBooks = aladinApiClient.searchBooksByCategory(
                categoryMapping.aladinCategoryId,
                categoryMapping.searchTarget,
                MAX_BOOKS_TO_FETCH
            )

            if (apiBooks.isEmpty()) {
                log.warn("알라딘 API에서 {} 카테고리의 책을 찾지 못했습니다", categoryMapping.categoryName)
                return
            }

            log.info("알라딘 API에서 {}권의 책을 조회했습니다", apiBooks.size)

            // API 결과를 엔티티로 변환하고 저장
            val savedBooks = apiBooks.mapNotNull { apiBook ->
                convertAndSaveBook(apiBook)?.also { savedBook ->
                    // 카테고리 이름이 다르면 업데이트 (알라딘 카테고리와 DB 카테고리 매핑)
                    if (savedBook.category.name != categoryMapping.categoryName) {
                        val targetCategory = categoryRepository.findByName(categoryMapping.categoryName)
                            ?: categoryRepository.save(Category(categoryMapping.categoryName))

                        savedBook.category = targetCategory
                        bookRepository.save(savedBook)
                        log.debug("책 카테고리 업데이트: {} -> {}", savedBook.title, categoryMapping.categoryName)
                    }
                }
            }

            // 상세 정보 보완
            enrichMissingDetails(savedBooks.toMutableList())

            log.info("알라딘 API에서 {} 카테고리로 {}권의 책을 성공적으로 저장했습니다",
                categoryMapping.categoryName, savedBooks.size)

        } catch (e: Exception) {
            log.error("알라딘 API에서 카테고리별 책 데이터 가져오기 실패: category={}, error={}",
                categoryMapping.categoryName, e.message)
        }
    }


    /**
     * 페이징 지원 검색 메서드 - DB에서 직접 정렬 처리
     */
    @Transactional
    fun searchBooks(query: String, pageable: Pageable, member: Member? = null): Page<BookSearchDto> {
        // 1. DB에서 페이징과 정렬이 적용된 검색 시도
        val dbResults = bookRepository.findValidBooksByTitleOrAuthorContainingWithPaging(query, pageable)

        if (dbResults.hasContent()) {
            log.info("DB에서 찾은 유효한 책: {} 권 (전체: {})", dbResults.numberOfElements, dbResults.totalElements)
            return dbResults.map { convertToDto(it, member) }
        }

        log.info("DB에 유효한 책이 없어서 알라딘 API에서 검색: {}", query)

        // 2. API에서 검색하여 DB에 저장 (충분한 양)
        val apiBooks = aladinApiClient.searchBooks(query, 100)

        // 3. API 결과를 엔티티로 변환하고 저장
        val savedBooks = apiBooks
            .mapNotNull { convertAndSaveBook(it) }
            .toMutableList()

        // 4. 상세 정보 보완
        enrichMissingDetails(savedBooks)

        // 5. DB에서 다시 페이징과 정렬이 적용된 검색
        log.info("API 검색 및 저장 완료. DB에서 다시 검색하여 페이징 처리")
        val finalResults = bookRepository.findValidBooksByTitleOrAuthorContainingWithPaging(query, pageable)

        if (!finalResults.hasContent()) {
            log.warn("API 검색 후에도 유효한 책이 없음: {}", query)
            return Page.empty(pageable)
        }

        return finalResults.map { convertToDto(it, member) }
    }

    /**
     * 기존 limit 방식 검색 메서드 (하위 호환성 유지)
     */
    @Transactional
    fun searchBooks(query: String, limit: Int, member: Member? = null): List<BookSearchDto> {
        // 1. DB에서 유효한 책들만 먼저 확인
        val validBooksFromDb = bookRepository.findValidBooksByTitleOrAuthorContaining(query)

        if (validBooksFromDb.isNotEmpty()) {
            log.info("DB에서 찾은 유효한 책: {} 권", validBooksFromDb.size)
            return convertToDto(validBooksFromDb.take(limit), member)
        }

        log.info("DB에 유효한 책이 없어서 알라딘 API에서 검색: {}", query)

        // 2. API에서 검색
        val apiBooks = aladinApiClient.searchBooks(query, limit)

        // 3. API 결과를 엔티티로 변환하고 저장
        val savedBooks = apiBooks
            .mapNotNull { convertAndSaveBook(it) }
            .toMutableList()

        // 4. 상세 정보 보완
        enrichMissingDetails(savedBooks)

        return convertToDto(savedBooks, member)
    }

    /**
     * ISBN으로 책 조회 - DB에 없으면 API에서 가져와서 저장
     */
    @Transactional
    fun getBookByIsbn(isbn: String, member: Member? = null): BookSearchDto? {
        // 먼저 유효한 책(페이지 수 > 0)이 있는지 확인
        val validBookFromDb = bookRepository.findValidBookByIsbn13(isbn)

        if (validBookFromDb != null) {
            log.info("DB에서 유효한 책 발견: {}", validBookFromDb.title)
            return convertToDto(validBookFromDb, member)
        }

        // 유효한 책이 없다면 기존 책(페이지 수 0 포함)이 있는지 확인
        val existingBookFromDb = bookRepository.findByIsbn13(isbn)

        if (existingBookFromDb != null) {
            log.info("DB에 페이지 수 0인 책이 있어서 상세 정보 보완 시도: {}", existingBookFromDb.title)

            // 상세 정보 보완 시도
            enrichBookWithDetailInfo(existingBookFromDb)

            // 보완 후에도 페이지 수가 0이면 null 반환
            if (existingBookFromDb.totalPage == 0) {
                log.warn("상세 정보 보완 후에도 페이지 수가 0인 책: {}", existingBookFromDb.title)
                return null
            }

            return convertToDto(existingBookFromDb, member)
        }

        // API에서 검색
        log.info("DB에 없어서 API에서 검색: {}", isbn)
        val apiBook = aladinApiClient.getBookByIsbn(isbn) ?: return null

        val savedBook = convertAndSaveBook(apiBook) ?: return null

        // 상세 정보 보완
        enrichBookWithDetailInfo(savedBook)

        // 페이지 수가 0이면 null 반환
        if (savedBook.totalPage == 0) {
            log.warn("새로 저장한 책의 페이지 수가 0: {}", savedBook.title)
            return null
        }

        return convertToDto(savedBook, member)
    }

    /**
     * 전체 책 조회 (페이징, Member 정보 포함) - DB에서 직접 정렬 처리
     */
    fun getAllBooks(pageable: Pageable, member: Member? = null): Page<BookSearchDto> {
        log.info(
            "전체 유효한 책 조회: page={}, size={}, sort={}, member={}",
            pageable.pageNumber, pageable.pageSize, pageable.sort,
            member?.id ?: "null"
        )

        return try {
            // DB에서 페이징과 정렬이 적용된 조회 (페이지 수가 0보다 큰 유효한 책들만)
            val validBookPage = bookRepository.findAllValidBooks(pageable)

            validBookPage.map { book ->
                val dto = convertToDto(book, member)
                log.debug("Book {} converted with readState: {}", book.id, dto.readState)
                dto
            }
        } catch (e: Exception) {
            log.error("전체 책 조회 중 오류 발생: {}", e.message)
            throw ServiceException("500-1", "전체 책 조회 중 오류가 발생했습니다.")
        }
    }

    /**
     * ID로 책 상세 정보 조회 (리뷰 페이징 포함)
     */
    @Transactional(readOnly = true)
    fun getBookDetailById(id: Int, pageable: Pageable, member: Member? = null): BookDetailDto? {
        log.info(
            "책 상세 조회: id={}, page={}, size={}, member={}",
            id, pageable.pageNumber, pageable.pageSize,
            member?.id ?: "null"
        )

        return try {
            // 책 조회 (페이지 수 0인 책도 상세 조회는 가능)
            val book = bookRepository.findById(id).orElse(null) ?: return null

            // 리뷰 페이징 조회
            val reviewPage = reviewRepository.findByBookOrderByCreateDateDesc(book, pageable)
            val reviewPageResponse = PageResponseDto(
                reviewPage.map { reviewDtoService.reviewToReviewResponseDto(it, member) }
            )

            // ReadState 조회
            val readState = member?.let { getReadStateByMemberAndBook(it, book) }

            BookDetailDto(
                id = book.id.toLong(),
                title = book.title,
                imageUrl = book.imageUrl,
                publisher = book.publisher,
                isbn13 = book.isbn13,
                totalPage = book.totalPage,
                publishedDate = book.publishedDate,
                avgRate = book.avgRate,
                categoryName = book.category.name,
                authors = book.authors.map { it.author.name },
                readState = readState,
                reviews = reviewPageResponse
            )
        } catch (e: Exception) {
            log.error("책 상세 조회 중 오류 발생: {}", e.message)
            throw ServiceException("500-2", "책 상세 조회 중 오류가 발생했습니다.")
        }
    }

    /**
     * 책 평균 평점 업데이트
     */
    fun updateBookAvgRate(book: Book) {
        val avgRate = calculateAvgRateForBook(book)
        book.avgRate = avgRate
        log.info("책 평균 평점 업데이트: {} -> {}", book.title, avgRate)
    }

    /**
     * Member와 Book으로 ReadState 조회
     */
    private fun getReadStateByMemberAndBook(member: Member, book: Book): ReadState? {
        log.debug("ReadState 조회: memberId={}, bookId={}", member.id, book.id)

        val bookmark = bookmarkRepository.findByMemberAndBook(member, book)
        val readState = bookmark.orElse(null)?.readState

        log.debug("ReadState 결과: {}", readState)
        return readState
    }

    /**
     * 여러 책들의 ReadState를 한번에 조회 (성능 최적화)
     */
    private fun getReadStatesForBooks(member: Member, bookIds: List<Int>): Map<Int, ReadState> {
        val bookmarks = bookmarkRepository.findByMemberAndBookIds(member, bookIds.toMutableList())
        return bookmarks.associate { it.book.id to it.readState }
    }

    // ==== Private Methods ====

    /**
     * AladinBookDto를 Book 엔티티로 변환하고 저장
     */
    private fun convertAndSaveBook(apiBook: AladinBookDto): Book? {
        return try {
            // 이미 존재하는 책인지 확인
            apiBook.isbn13?.let { isbn ->
                bookRepository.findByIsbn13(isbn)?.let { existingBook ->
                    log.info("이미 존재하는 ISBN: {}", isbn)
                    return existingBook
                }
            }

            // 카테고리 설정
            val categoryName = extractCategoryFromPath(apiBook.categoryName, apiBook.mallType)
            val category = categoryRepository.findByName(categoryName)
                ?: categoryRepository.save(Category(categoryName)).also {
                    log.info("새 카테고리 생성: {}", categoryName)
                }

            // Book 엔티티 생성
            val book = Book(
                title = apiBook.title ?: "",
                publisher = apiBook.publisher,
                category = category
            ).apply {
                imageUrl = apiBook.imageUrl
                isbn13 = apiBook.isbn13
                totalPage = apiBook.totalPage
                publishedDate = apiBook.publishedDate
                avgRate = 0.0f
            }

            // 책 저장
            val savedBook = bookRepository.save(book)
            log.info("책 저장 완료: {} (페이지: {})", savedBook.title, savedBook.totalPage)

            // 작가 정보 저장
            saveAuthors(apiBook.authors, savedBook)

            savedBook
        } catch (e: Exception) {
            log.error("책 저장 중 오류: {}", e.message)
            null
        }
    }

    /**
     * 작가 정보 저장
     */
    private fun saveAuthors(authorNames: List<String>?, book: Book) {
        if (authorNames.isNullOrEmpty()) return

        authorNames.forEach { authorName ->
            try {
                // 작가가 이미 존재하는지 확인
                val author = authorRepository.findByName(authorName)
                    ?: authorRepository.save(Author(authorName))

                // 이미 이 책과 작가의 관계가 존재하는지 확인
                if (!wroteRepository.existsByAuthorAndBook(author, book)) {
                    val wrote = Wrote(author, book)
                    wroteRepository.save(wrote)
                    log.info("작가-책 관계 저장: {} - {}", authorName, book.title)
                }
            } catch (e: Exception) {
                log.error("작가 정보 저장 중 오류: {} - {}", authorName, e.message)
            }
        }
    }

    /**
     * 부족한 상세 정보 보완 및 불완전한 데이터 필터링
     */
    private fun enrichMissingDetails(books: MutableList<Book>): List<Book> {
        val incompleteBooks = mutableListOf<Book>()

        val enrichedBooks = books.mapNotNull { book ->
            if (needsDetailEnrichment(book)) {
                log.info("상세 정보 보완 시도: {} (ISBN: {})", book.title, book.isbn13)
                enrichBookWithDetailInfo(book)
            }

            // 상세 정보 보완 후에도 페이지 수가 0인 책은 제외
            if (book.totalPage == 0) {
                log.warn("페이지 수가 0인 책을 목록에서 제외: {} (ISBN: {})", book.title, book.isbn13)
                incompleteBooks.add(book)
                null
            } else {
                book
            }
        }

        // 불완전한 책들의 정보를 로그로 남김
        if (incompleteBooks.isNotEmpty()) {
            log.info("불완전한 책 {}권이 발견되어 검색 결과에서 제외됨", incompleteBooks.size)
            incompleteBooks.forEach { book ->
                log.warn("정리 대상 책 ID: {} - {} (페이지: {})", book.id, book.title, book.totalPage)
            }
        }

        return enrichedBooks
    }

    /**
     * 상세 정보 보완이 필요한지 판단
     */
    private fun needsDetailEnrichment(book: Book): Boolean {
        return book.isbn13 != null && (book.totalPage == 0 || book.authors.isEmpty())
    }

    /**
     * 개별 ISBN 조회로 상세 정보 보완
     */
    private fun enrichBookWithDetailInfo(book: Book) {
        val detailBook = aladinApiClient.getBookDetails(book.isbn13)
        if (detailBook == null) {
            log.warn("상세 정보 조회 실패: {} (ISBN: {})", book.title, book.isbn13)
            return
        }

        var updated = false

        // 페이지 수 보완
        if (book.totalPage == 0 && detailBook.totalPage > 0) {
            book.totalPage = detailBook.totalPage
            updated = true
            log.info("페이지 수 보완: {} -> {}", book.title, detailBook.totalPage)
        }

        // 저자 정보 보완
        if (book.authors.isEmpty() && !detailBook.authors.isNullOrEmpty()) {
            saveAuthors(detailBook.authors, book)
            updated = true
            log.info("저자 정보 보완: {} -> {}", book.title, detailBook.authors)
        }

        if (updated) {
            bookRepository.save(book)
            log.info("상세 정보 보완 완료: {} (페이지: {}, 저자: {})", book.title, book.totalPage, book.authors.size)
        }
    }

    /**
     * 카테고리 경로에서 2번째 깊이 추출
     */
    private fun extractCategoryFromPath(categoryName: String?, mallType: String?): String {
        if (!categoryName.isNullOrEmpty()) {
            log.debug("원본 카테고리 경로: {}", categoryName)

            val categoryParts = categoryName.split(">")

            if (categoryParts.size > 1) {
                val secondLevelCategory = categoryParts[1].trim()
                log.debug("추출된 카테고리: {}", secondLevelCategory)
                return secondLevelCategory
            }

            if (categoryParts.isNotEmpty()) {
                val firstLevelCategory = categoryParts[0].trim()
                log.debug("첫 번째 레벨 카테고리 사용: {}", firstLevelCategory)
                return firstLevelCategory
            }
        }

        // 기본 카테고리
        val fallbackCategory = getFallbackCategory(mallType)
        log.debug("기본 카테고리 사용: {}", fallbackCategory)
        return fallbackCategory
    }

    /**
     * mallType 기반 기본 카테고리
     */
    private fun getFallbackCategory(mallType: String?): String {
        return when (mallType) {
            "BOOK" -> "국내도서"
            "FOREIGN" -> "외국도서"
            else -> "기타"
        }
    }

    /**
     * 책의 평균 평점 계산
     */
    private fun calculateAvgRateForBook(book: Book): Float {
        val reviews = book.reviews
        if (reviews.isEmpty()) return 0.0f

        return reviews.filter{ !it.deleted}.map { it.rate }.average().toFloat()
    }

    /**
     * Book 엔티티 리스트를 DTO로 변환 (Member 정보 포함)
     */
    private fun convertToDto(books: List<Book>, member: Member?): List<BookSearchDto> {
        if (member == null) {
            return books.map { convertToDto(it, null as ReadState?) }
        }

        // 성능 최적화: 한번에 모든 책의 ReadState 조회
        val bookIds = books.map { it.id }
        val readStatesMap = getReadStatesForBooks(member, bookIds)

        return books.map { book ->
            val readState = readStatesMap[book.id]
            convertToDto(book, readState)
        }
    }

    /**
     * 단일 Book 엔티티를 DTO로 변환 (Member 정보 포함)
     */
    private fun convertToDto(book: Book, member: Member?): BookSearchDto {
        val readState = member?.let { getReadStateByMemberAndBook(it, book) }
        return convertToDto(book, readState)
    }

    /**
     * 단일 Book 엔티티를 DTO로 변환 (ReadState 직접 전달)
     */
    private fun convertToDto(book: Book, readState: ReadState?): BookSearchDto {
        return BookSearchDto(
            id = book.id.toLong(),
            title = book.title,
            imageUrl = book.imageUrl,
            publisher = book.publisher,
            isbn13 = book.isbn13,
            totalPage = book.totalPage,
            publishedDate = book.publishedDate,
            avgRate = book.avgRate,
            categoryName = book.category.name,
            authors = book.authors.map { it.author.name },
            readState = readState
        )
    }
}