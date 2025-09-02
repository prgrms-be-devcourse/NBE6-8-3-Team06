package com.back.domain.book.book.controller

import com.back.domain.book.author.entity.Author
import com.back.domain.book.author.repository.AuthorRepository
import com.back.domain.book.book.entity.Book
import com.back.domain.book.book.repository.BookRepository
import com.back.domain.book.category.entity.Category
import com.back.domain.book.category.repository.CategoryRepository
import com.back.domain.book.wrote.entity.Wrote
import com.back.domain.book.wrote.repository.WroteRepository
import com.back.domain.bookmarks.constant.ReadState
import com.back.domain.bookmarks.entity.Bookmark
import com.back.domain.bookmarks.repository.BookmarkRepository
import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.repository.MemberRepository
import com.back.domain.review.review.entity.Review
import com.back.domain.review.review.repository.ReviewRepository
import com.back.global.security.SecurityUser
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
internal class BookControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var authorRepository: AuthorRepository

    @Autowired
    private lateinit var wroteRepository: WroteRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var bookmarkRepository: BookmarkRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var reviewRepository: ReviewRepository

    private lateinit var testMember: Member
    private lateinit var book1: Book
    private lateinit var book2: Book
    private lateinit var review1: Review
    private lateinit var review2: Review

    @BeforeEach
    fun setUp() {
        // 테스트용 멤버 생성
        testMember = memberRepository.save(
            Member("testuser", "test@example.com", "password123")
        )

        // 테스트용 데이터 셋업
        val category = categoryRepository.save(Category("소설"))
        val author = authorRepository.save(Author("김작가"))

        book1 = bookRepository.save(
            Book("테스트 책 1", "테스트 출판사", category).apply {
                isbn13 = "9780123456789"
                imageUrl = "https://example.com/book1.jpg"
                totalPage = 200
                publishedDate = LocalDateTime.of(2023, 1, 1, 0, 0)
                avgRate = 4.5f
            }
        )

        book2 = bookRepository.save(
            Book("테스트 책 2", "다른 출판사", category).apply {
                isbn13 = "9780987654321"
                imageUrl = "https://example.com/book2.jpg"
                totalPage = 300
                publishedDate = LocalDateTime.of(2023, 6, 15, 0, 0)
                avgRate = 3.8f
            }
        )

        // 작가-책 관계 설정
        wroteRepository.saveAll(
            listOf(
                Wrote(author, book1),
                Wrote(author, book2)
            )
        )

        // 북마크 설정 (book1은 READING, book2는 북마크 안함)
        bookmarkRepository.save(
            Bookmark(book1, testMember).apply {
                updateReadState(ReadState.READING)
                updateReadPage(50)
            }
        )

        // 리뷰 생성
        review1 = reviewRepository.save(Review("정말 좋은 책입니다!", 5, false, testMember, book1))
        review2 = reviewRepository.save(Review("보통이네요", 3, false, testMember, book1))

        // 영속성 컨텍스트 플러시 및 클리어
        entityManager.run {
            flush()
            clear()
        }
    }

    @Test
    @DisplayName("전체 책 조회 - 로그인하지 않은 사용자 (readState null)")
    fun getAllBooks_NotLoggedIn_Success() {
        mockMvc.get("/books") {
            param("page", "0")
            param("size", "10")
            param("sortBy", "id")
            param("sortDir", "desc")
        }.andExpect {
            status { isOk() }
            jsonPath("$.resultCode") { value("200-1") }
            jsonPath("$.msg") { value("전체 책 조회 성공") }
            jsonPath("$.data.data") { isArray() }
            jsonPath("$.data.data.length()") { value(10) }
            jsonPath("$.data.data[0].title") { exists() }
            jsonPath("$.data.data[0].publisher") { exists() }
            jsonPath("$.data.data[0].categoryName") { value("소설") }
            jsonPath("$.data.data[0].authors") { isArray() }
            jsonPath("$.data.data[0].authors[0]") { value("김작가") }
            jsonPath("$.data.data[0].readState") { doesNotExist() }
            jsonPath("$.data.data[1].readState") { doesNotExist() }
        }
    }

    @Test
    @DisplayName("전체 책 조회 - 로그인한 사용자 (readState 포함)")
    fun getAllBooks_LoggedIn_Success() {
        println("=== 테스트 시작 ===")
        println("테스트 멤버 ID: ${testMember.id}")
        println("Book1 ID: ${book1.id}")
        println("Book2 ID: ${book2.id}")

        // 북마크 확인
        val bookmarks = bookmarkRepository.findByMember(testMember)
        println("북마크 수: ${bookmarks.size}")
        bookmarks.forEach { bookmark ->
            println("북마크 - 책 ID: ${bookmark.book.id}, ReadState: ${bookmark.readState}")
        }

        mockMvc.get("/books") {
            with(user(SecurityUser(testMember)))
            param("page", "0")
            param("size", "10")
            param("sortBy", "id")
            param("sortDir", "desc")
        }.andDo { print() }
            .andExpect {
                status { isOk() }
                jsonPath("$.resultCode") { value("200-1") }
                jsonPath("$.msg") { value("전체 책 조회 성공") }
                jsonPath("$.data.data") { isArray() }
                jsonPath("$.data.data.length()") { value(10) }
            }
    }

    @Test
    @DisplayName("책 검색 - 로그인하지 않은 사용자 (readState null)")
    fun searchBooks_NotLoggedIn_Success() {
        mockMvc.get("/books/search") {
            param("query", "테스트 책")
            param("page", "0")
            param("size", "20")
            param("sortBy", "id")
            param("sortDir", "desc")
        }.andExpect {
            status { isOk() }
            jsonPath("$.resultCode") { value("200-1") }
            jsonPath("$.msg") { value("2개의 책을 찾았습니다.") }
            jsonPath("$.data") { isNotEmpty() }
            jsonPath("$.data.data") { isArray() }
            jsonPath("$.data.data.length()") { value(2) }
            jsonPath("$.data.data[0].title") { exists() }
            jsonPath("$.data.data[0].publisher") { exists() }
            jsonPath("$.data.data[0].categoryName") { value("소설") }
            jsonPath("$.data.data[0].authors") { isArray() }
            jsonPath("$.data.data[0].authors[0]") { value("김작가") }
            jsonPath("$.data.data[0].isbn13") { exists() }
            jsonPath("$.data.data[0].totalPage") { exists() }
            jsonPath("$.data.data[0].avgRate") { exists() }
            jsonPath("$.data.data[0].readState") { doesNotExist() }
            jsonPath("$.data.data[1].readState") { doesNotExist() }
            jsonPath("$.data.totalElements") { value(2) }
            jsonPath("$.data.totalPages") { value(1) }
            jsonPath("$.data.pageSize") { value(20) }
            jsonPath("$.data.pageNumber") { value(0) }
            jsonPath("$.data.isLast") { value(true) }
        }
    }

    @Test
    @DisplayName("책 검색 - 로그인한 사용자 (readState 포함)")
    fun searchBooks_LoggedIn_Success() {
        mockMvc.get("/books/search") {
            with(user(SecurityUser(testMember)))
            param("query", "테스트 책")
            param("page", "0")
            param("size", "20")
            param("sortBy", "id")
            param("sortDir", "desc")
        }.andExpect {
            status { isOk() }
            jsonPath("$.resultCode") { value("200-1") }
            jsonPath("$.msg") { value("2개의 책을 찾았습니다.") }
            jsonPath("$.data") { isNotEmpty() }
            jsonPath("$.data.data") { isArray() }
            jsonPath("$.data.data.length()") { value(2) }
            jsonPath("$.data.totalElements") { value(2) }
        }
    }

    @Test
    @DisplayName("책 검색 - 작가명으로 검색 성공")
    fun searchBooks_ByAuthor_Success() {
        mockMvc.get("/books/search") {
            param("query", "김작가")
            param("page", "0")
            param("size", "20")
            param("sortBy", "id")
            param("sortDir", "desc")
        }.andExpect {
            status { isOk() }
            jsonPath("$.resultCode") { value("200-1") }
            jsonPath("$.msg") { value("2개의 책을 찾았습니다.") }
            jsonPath("$.data.data") { isArray() }
            jsonPath("$.data.data.length()") { value(2) }
            jsonPath("$.data.data[0].authors") { isArray() }
            jsonPath("$.data.data[0].authors[0]") { value("김작가") }
            jsonPath("$.data.data[1].authors") { isArray() }
            jsonPath("$.data.data[1].authors[0]") { value("김작가") }
            jsonPath("$.data.totalElements") { value(2) }
        }
    }

    @Test
    @DisplayName("책 검색 - 부분 제목으로 검색 성공")
    fun searchBooks_ByPartialTitle_Success() {
        mockMvc.get("/books/search") {
            param("query", "책 1")
            param("page", "0")
            param("size", "20")
            param("sortBy", "id")
            param("sortDir", "desc")
        }.andExpect {
            status { isOk() }
            jsonPath("$.resultCode") { value("200-1") }
            jsonPath("$.msg") { value("1개의 책을 찾았습니다.") }
            jsonPath("$.data.data") { isArray() }
            jsonPath("$.data.data.length()") { value(1) }
            jsonPath("$.data.data[0].title") { value("테스트 책 1") }
            jsonPath("$.data.data[0].id") { exists() }
            jsonPath("$.data.data[0].imageUrl") { exists() }
            jsonPath("$.data.data[0].publishedDate") { exists() }
            jsonPath("$.data.totalElements") { value(1) }
        }
    }

    @Test
    @DisplayName("책 검색 - size 제한 테스트")
    fun searchBooks_WithSizeRestriction_Success() {
        mockMvc.get("/books/search") {
            param("query", "테스트")
            param("page", "0")
            param("size", "1")
            param("sortBy", "id")
            param("sortDir", "desc")
        }.andExpect {
            status { isOk() }
            jsonPath("$.resultCode") { value("200-1") }
            jsonPath("$.msg") { value("2개의 책을 찾았습니다.") } // 전체 결과 수
            jsonPath("$.data.data") { isArray() }
            jsonPath("$.data.data.length()") { value(1) } // 페이지 크기만큼만
            jsonPath("$.data.totalElements") { value(2) }
            jsonPath("$.data.pageSize") { value(1) }
        }
    }

    @Test
    @DisplayName("책 검색 - 검색 결과 없음")
    fun searchBooks_NoResults_Success() {
        mockMvc.get("/books/search") {
            param("query", "존재하지않는책")
            param("page", "0")
            param("size", "20")
            param("sortBy", "id")
            param("sortDir", "desc")
        }.andExpect {
            status { isOk() }
            jsonPath("$.resultCode") { value("200-2") }
            jsonPath("$.msg") { value("검색 결과가 없습니다.") }
            jsonPath("$.data.data") { isArray() }
            jsonPath("$.data.data.length()") { value(0) }
            jsonPath("$.data.totalElements") { value(0) }
            jsonPath("$.data.totalPages") { value(0) }
        }
    }

    @Test
    @DisplayName("책 검색 실패 - query 파라미터 누락")
    fun searchBooks_Fail_MissingQuery() {
        mockMvc.get("/books/search") {
            param("limit", "20")
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    @DisplayName("책 검색 실패 - 빈 query")
    fun searchBooks_Fail_EmptyQuery() {
        mockMvc.get("/books/search") {
            param("query", "")
            param("limit", "20")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.resultCode") { exists() }
            jsonPath("$.msg") { exists() }
        }
    }

    @Test
    @DisplayName("책 검색 실패 - 공백만 있는 query")
    fun searchBooks_Fail_WhitespaceOnlyQuery() {
        mockMvc.get("/books/search") {
            param("query", "   ")
            param("limit", "20")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.resultCode") { exists() }
            jsonPath("$.msg") { exists() }
        }
    }

    @Test
    @DisplayName("ISBN 검색 - 로그인하지 않은 사용자 (readState null)")
    fun getBookByIsbn_NotLoggedIn_Success() {
        mockMvc.get("/books/isbn/9780123456789")
            .andExpect {
                status { isOk() }
                jsonPath("$.resultCode") { value("200-3") }
                jsonPath("$.msg") { value("ISBN으로 책 조회 성공") }
                jsonPath("$.data") { isNotEmpty() }
                jsonPath("$.data.title") { value("테스트 책 1") }
                jsonPath("$.data.publisher") { value("테스트 출판사") }
                jsonPath("$.data.isbn13") { value("9780123456789") }
                jsonPath("$.data.totalPage") { value(200) }
                jsonPath("$.data.avgRate") { value(4.5) }
                jsonPath("$.data.categoryName") { value("소설") }
                jsonPath("$.data.authors") { isArray() }
                jsonPath("$.data.authors[0]") { value("김작가") }
                jsonPath("$.data.imageUrl") { value("https://example.com/book1.jpg") }
                jsonPath("$.data.publishedDate") { exists() }
                jsonPath("$.data.readState") { doesNotExist() }
            }
    }

    @Test
    @DisplayName("ISBN 검색 - 로그인한 사용자 (readState 포함)")
    fun getBookByIsbn_LoggedIn_Success() {
        mockMvc.get("/books/isbn/9780123456789") {
            with(user(SecurityUser(testMember)))
        }.andExpect {
            status { isOk() }
            jsonPath("$.resultCode") { value("200-3") }
            jsonPath("$.msg") { value("ISBN으로 책 조회 성공") }
            jsonPath("$.data") { isNotEmpty() }
            jsonPath("$.data.title") { value("테스트 책 1") }
            jsonPath("$.data.readState") { value("READING") }
        }
    }

    @Test
    @DisplayName("ISBN 검색 - 하이픈이 포함된 ISBN으로 조회 성공")
    fun getBookByIsbn_WithHyphens_Success() {
        mockMvc.get("/books/isbn/978-0-123-45678-9")
            .andExpect {
                status { isOk() }
                jsonPath("$.resultCode") { value("200-3") }
                jsonPath("$.msg") { value("ISBN으로 책 조회 성공") }
                jsonPath("$.data") { isNotEmpty() }
                jsonPath("$.data.title") { value("테스트 책 1") }
                jsonPath("$.data.isbn13") { value("9780123456789") }
            }
    }

    @Test
    @DisplayName("ISBN 검색 실패 - 빈 ISBN")
    fun getBookByIsbn_Fail_EmptyIsbn() {
        mockMvc.get("/books/isbn/")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @DisplayName("ISBN 검색 실패 - 잘못된 ISBN 형식 (12자리)")
    fun getBookByIsbn_Fail_InvalidFormat_12Digits() {
        mockMvc.get("/books/isbn/123456789012")
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.resultCode") { value("400-8") }
                jsonPath("$.msg") { value("올바른 ISBN-13 형식이 아닙니다. (13자리 숫자)") }
            }
    }

    @Test
    @DisplayName("ISBN 검색 실패 - 잘못된 ISBN 형식 (14자리)")
    fun getBookByIsbn_Fail_InvalidFormat_14Digits() {
        mockMvc.get("/books/isbn/12345678901234")
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.resultCode") { value("400-8") }
                jsonPath("$.msg") { value("올바른 ISBN-13 형식이 아닙니다. (13자리 숫자)") }
            }
    }

    @Test
    @DisplayName("책 상세 조회 - 로그인하지 않은 사용자 (readState null)")
    fun getBookById_NotLoggedIn_Success() {
        mockMvc.get("/books/${book1.id}") {
            param("page", "0")
            param("size", "10")
            param("sortBy", "id")
            param("sortDir", "desc")
        }.andExpect {
            status { isOk() }
            jsonPath("$.resultCode") { value("200-4") }
            jsonPath("$.msg") { value("책 상세 조회 성공") }
            jsonPath("$.data") { isNotEmpty() }
            jsonPath("$.data.id") { value(book1.id) }
            jsonPath("$.data.title") { value("테스트 책 1") }
            jsonPath("$.data.publisher") { value("테스트 출판사") }
            jsonPath("$.data.isbn13") { value("9780123456789") }
            jsonPath("$.data.totalPage") { value(200) }
            jsonPath("$.data.avgRate") { value(4.5) }
            jsonPath("$.data.categoryName") { value("소설") }
            jsonPath("$.data.authors") { isArray() }
            jsonPath("$.data.authors[0]") { value("김작가") }
            jsonPath("$.data.imageUrl") { value("https://example.com/book1.jpg") }
            jsonPath("$.data.publishedDate") { exists() }
            jsonPath("$.data.readState") { doesNotExist() }
            jsonPath("$.data.reviews") { isNotEmpty() }
            jsonPath("$.data.reviews.data") { isArray() }
            jsonPath("$.data.reviews.data.length()") { value(2) }
            jsonPath("$.data.reviews.totalElements") { value(2) }
            jsonPath("$.data.reviews.totalPages") { value(1) }
            jsonPath("$.data.reviews.pageSize") { value(10) }
            jsonPath("$.data.reviews.pageNumber") { value(0) }
            jsonPath("$.data.reviews.isLast") { value(true) }
        }
    }

    @Test
    @DisplayName("책 상세 조회 - 로그인한 사용자 (readState 포함)")
    fun getBookById_LoggedIn_Success() {
        mockMvc.get("/books/${book1.id}") {
            with(user(SecurityUser(testMember)))
            param("page", "0")
            param("size", "10")
            param("sortBy", "id")
            param("sortDir", "desc")
        }.andExpect {
            status { isOk() }
            jsonPath("$.resultCode") { value("200-4") }
            jsonPath("$.msg") { value("책 상세 조회 성공") }
            jsonPath("$.data") { isNotEmpty() }
            jsonPath("$.data.id") { value(book1.id) }
            jsonPath("$.data.title") { value("테스트 책 1") }
            jsonPath("$.data.readState") { value("READING") }
            jsonPath("$.data.reviews") { isNotEmpty() }
            jsonPath("$.data.reviews.data") { isArray() }
            jsonPath("$.data.reviews.data.length()") { value(2) }
        }
    }

    @Test
    @DisplayName("책 상세 조회 - 리뷰 내용 검증")
    fun getBookById_ReviewContent_Success() {
        mockMvc.get("/books/${book1.id}") {
            with(user(SecurityUser(testMember)))
            param("page", "0")
            param("size", "10")
            param("sortBy", "id")
            param("sortDir", "desc")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.reviews.data[0].id") { exists() }
            jsonPath("$.data.reviews.data[0].content") { exists() }
            jsonPath("$.data.reviews.data[0].rate") { exists() }
            jsonPath("$.data.reviews.data[0].memberName") { exists() }
            jsonPath("$.data.reviews.data[0].memberId") { value(testMember.id) }
            jsonPath("$.data.reviews.data[0].likeCount") { exists() }
            jsonPath("$.data.reviews.data[0].dislikeCount") { exists() }
            jsonPath("$.data.reviews.data[0].createdDate") { exists() }
            jsonPath("$.data.reviews.data[0].modifiedDate") { exists() }
        }
    }

    @Test
    @DisplayName("책 상세 조회 - 리뷰 페이징 테스트")
    fun getBookById_ReviewPaging_Success() {
        // 첫 번째 페이지
        mockMvc.get("/books/${book1.id}") {
            param("page", "0")
            param("size", "1")
            param("sortBy", "id")
            param("sortDir", "desc")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.reviews.data.length()") { value(1) }
            jsonPath("$.data.reviews.totalElements") { value(2) }
            jsonPath("$.data.reviews.totalPages") { value(2) }
            jsonPath("$.data.reviews.pageSize") { value(1) }
            jsonPath("$.data.reviews.pageNumber") { value(0) }
            jsonPath("$.data.reviews.isLast") { value(false) }
        }

        // 두 번째 페이지
        mockMvc.get("/books/${book1.id}") {
            param("page", "1")
            param("size", "1")
            param("sortBy", "id")
            param("sortDir", "desc")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.reviews.data.length()") { value(1) }
            jsonPath("$.data.reviews.pageNumber") { value(1) }
            jsonPath("$.data.reviews.isLast") { value(true) }
        }
    }

    @Test
    @DisplayName("책 상세 조회 - 리뷰가 없는 책")
    fun getBookById_NoReviews_Success() {
        mockMvc.get("/books/${book2.id}") {
            param("page", "0")
            param("size", "10")
            param("sortBy", "id")
            param("sortDir", "desc")
        }.andExpect {
            status { isOk() }
            jsonPath("$.resultCode") { value("200-4") }
            jsonPath("$.data.id") { value(book2.id) }
            jsonPath("$.data.title") { value("테스트 책 2") }
            jsonPath("$.data.reviews") { isNotEmpty() }
            jsonPath("$.data.reviews.data") { isArray() }
            jsonPath("$.data.reviews.data.length()") { value(0) }
            jsonPath("$.data.reviews.totalElements") { value(0) }
            jsonPath("$.data.reviews.totalPages") { value(0) }
        }
    }

    @Test
    @DisplayName("책 상세 조회 - 리뷰 정렬 테스트 (생성일시 내림차순)")
    fun getBookById_ReviewSortByCreatedDate_Success() {
        // 추가 리뷰 생성 (시간 차이를 두기 위해)
        Thread.sleep(1000)
        val newerReview = reviewRepository.save(Review("최신 리뷰입니다", 4, false, testMember, book1))

        entityManager.run {
            flush()
            clear()
        }

        mockMvc.get("/books/${book1.id}") {
            param("page", "0")
            param("size", "10")
            param("sortBy", "createDate")
            param("sortDir", "desc")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.reviews.data.length()") { value(3) }
            jsonPath("$.data.reviews.data[0].content") { value("최신 리뷰입니다") }
        }
    }

    @Test
    @DisplayName("검색 페이징 - 첫 번째 페이지 조회 성공")
    fun searchBooksWithPagination_FirstPage_Success() {
        mockMvc.get("/books/search") {
            param("query", "테스트 책")
            param("page", "0")
            param("size", "1")
            param("sortBy", "id")
            param("sortDir", "desc")
        }.andExpect {
            status { isOk() }
            jsonPath("$.resultCode") { value("200-1") }
            jsonPath("$.msg") { value("2개의 책을 찾았습니다.") }
            jsonPath("$.data") { isNotEmpty() }
            jsonPath("$.data.data") { isArray() }
            jsonPath("$.data.data.length()") { value(1) }
            jsonPath("$.data.totalElements") { value(2) }
            jsonPath("$.data.totalPages") { value(2) }
            jsonPath("$.data.pageSize") { value(1) }
            jsonPath("$.data.pageNumber") { value(0) }
            jsonPath("$.data.isLast") { value(false) }
            jsonPath("$.data.data[0].title") { exists() }
            jsonPath("$.data.data[0].authors") { isArray() }
        }
    }

    private fun setupSecurityContext() {
        val authentication = UsernamePasswordAuthenticationToken(testMember, null, null)
        SecurityContextHolder.getContext().authentication = authentication
    }
}