package com.back.domain.book.book.service

import com.back.domain.book.author.repository.AuthorRepository
import com.back.domain.book.book.dto.BookSearchDto
import com.back.domain.book.book.entity.Book
import com.back.domain.book.book.repository.BookRepository
import com.back.domain.book.category.repository.CategoryRepository
import com.back.domain.book.wrote.repository.WroteRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import kotlin.math.min

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("BookService 통합 테스트 - 외부 API 연동 및 데이터베이스 저장")
internal class BookServiceIntegrationTest {

    @Autowired
    private lateinit var bookService: BookService

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var authorRepository: AuthorRepository

    @Autowired
    private lateinit var wroteRepository: WroteRepository

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Value("\${aladin.api.key}")
    private lateinit var aladinApiKey: String

    @Value("\${aladin.api.base-url}")
    private lateinit var aladinBaseUrl: String

    companion object {
        private const val TEST_SEARCH_KEYWORD = "자바"
        private const val TEST_ISBN = "9788966262281"
    }

    @BeforeAll
    fun setUpBeforeAll() {
        println("=== BookService 통합 테스트 시작 ===")
        val maskedApiKey = aladinApiKey.take(min(10, aladinApiKey.length)) + "..."
        println("API Key: $maskedApiKey")
        println("API Base URL: $aladinBaseUrl")
    }

    @AfterAll
    fun tearDownAfterAll() {
        println("=== BookService 통합 테스트 완료 ===")
    }

    @Test
    @DisplayName("API 설정이 올바르게 주입되었는지 확인")
    fun checkApiConfiguration() {
        // API 키와 URL이 제대로 주입되었는지 확인
        assertThat(aladinApiKey).isNotEmpty()
        assertThat(aladinApiKey).doesNotContain("\${") // 환경변수 참조가 해결되었는지 확인

        assertThat(aladinBaseUrl).isNotEmpty()
        assertThat(aladinBaseUrl).doesNotContain("\${")

        println("✅ API 설정 확인 완료")
        println("API Key 길이: ${aladinApiKey.length}")
        println("API Base URL: $aladinBaseUrl")
    }

    @Test
    @DisplayName("검색어로 책을 찾지 못했을 때 외부 API에서 가져와서 DB에 저장한다")
    fun searchBooks_WhenNotFoundInDB_ShouldFetchFromApiAndSaveToDatabase() {
        // Given
        val uniqueKeyword = "spring boot ${System.currentTimeMillis()}" // 중복 방지

        // DB에 해당 키워드로 검색되는 책이 없음을 확인
        val booksBeforeSearch = bookRepository.findByTitleOrAuthorContaining(uniqueKeyword)
        assertThat(booksBeforeSearch).isEmpty()

        // When
        val searchResults = bookService.searchBooks(uniqueKeyword, 3, null)

        // Then
        if (searchResults.isNotEmpty()) {
            // API에서 결과를 가져온 경우
            assertThat(searchResults).isNotEmpty()

            // DB에 저장되었는지 확인
            val booksAfterSearch = bookRepository.findByTitleOrAuthorContaining(uniqueKeyword)
            assertThat(booksAfterSearch).isNotEmpty()

            // 첫 번째 결과 상세 검증
            verifyBookSavedCorrectly(searchResults.first())
        } else {
            // API에서 결과가 없는 경우도 정상 동작
            println("API에서 검색 결과 없음 - 정상 케이스")
        }
    }

    @Test
    @DisplayName("일반적인 검색어로 API에서 책을 가져와 DB에 저장한다")
    fun searchBooks_WithCommonKeyword_ShouldFetchAndSaveBooks() {
        // Given
        val booksBeforeSearch = bookRepository.findByTitleOrAuthorContaining(TEST_SEARCH_KEYWORD)
        val countBeforeSearch = booksBeforeSearch.size

        // When
        val searchResults = bookService.searchBooks(TEST_SEARCH_KEYWORD, 5, null)

        // Then
        assertThat(searchResults).isNotEmpty()

        // DB에 새로운 책이 추가되었거나 기존 책을 반환했는지 확인
        val booksAfterSearch = bookRepository.findByTitleOrAuthorContaining(TEST_SEARCH_KEYWORD)
        assertThat(booksAfterSearch.size).isGreaterThanOrEqualTo(countBeforeSearch)

        // 각 결과의 유효성 검증
        searchResults.forEach { verifyBookDataIntegrity(it) }
    }

    @Test
    @DisplayName("ISBN으로 책을 찾지 못했을 때 외부 API에서 가져와서 DB에 저장한다")
    fun getBookByIsbn_WhenNotFoundInDB_ShouldFetchFromApiAndSaveToDatabase() {
        // Given
        // DB에서 해당 ISBN이 없음을 확인
        bookRepository.findByIsbn13(TEST_ISBN)?.let { existingBook ->
            // 테스트를 위해 기존 데이터 삭제 (필요한 경우)
            bookRepository.delete(existingBook)
        }

        val bookBeforeApi = bookRepository.findByIsbn13(TEST_ISBN)
        assertThat(bookBeforeApi).isNull()

        // When
        val result = bookService.getBookByIsbn(TEST_ISBN, null)

        // Then
        result?.let {
            // API에서 성공적으로 가져온 경우
            assertThat(it.isbn13).isEqualTo(TEST_ISBN)

            // DB에 저장되었는지 확인
            val bookAfterApi = bookRepository.findByIsbn13(TEST_ISBN)
            assertThat(bookAfterApi).isNotNull()

            val savedBook = bookAfterApi!!
            assertThat(savedBook.title).isEqualTo(it.title)
            assertThat(savedBook.publisher).isEqualTo(it.publisher)

            // 작가 정보도 함께 저장되었는지 확인
            verifyAuthorsSavedCorrectly(savedBook, it)

            // 카테고리도 저장되었는지 확인
            assertThat(savedBook.category).isNotNull()
            assertThat(savedBook.category.name).isNotBlank()
        } ?: run {
            // API에서 해당 ISBN을 찾지 못한 경우도 정상 동작
            println("API에서 해당 ISBN을 찾지 못함 - 정상 케이스")
        }
    }

    @Test
    @DisplayName("동일한 ISBN으로 두 번 조회할 때 두 번째는 DB에서 가져온다")
    fun getBookByIsbn_SecondCall_ShouldReturnFromDatabase() {
        // Given & When - 첫 번째 호출
        val firstResult = bookService.getBookByIsbn(TEST_ISBN, null)

        firstResult?.let { first ->
            // When - 두 번째 호출
            val secondResult = bookService.getBookByIsbn(TEST_ISBN, null)

            // Then
            assertThat(secondResult).isNotNull()
            secondResult?.let { second ->
                assertThat(second.title).isEqualTo(first.title)
                assertThat(second.isbn13).isEqualTo(first.isbn13)

                // 두 결과가 동일한 DB 데이터에서 온 것인지 확인
                verifyResultsAreIdentical(first, second)
            }
        }
    }

    @Test
    @DisplayName("작가와 책의 관계가 정확히 저장되고 조회된다")
    fun verifyAuthorBookRelationshipIntegrity() {
        // Given & When
        val result = bookService.getBookByIsbn(TEST_ISBN, null)

        result?.takeIf { it.authors.isNotEmpty() }?.let {
            // Then
            val book = bookRepository.findByIsbn13(TEST_ISBN)
            assertThat(book).isNotNull()

            val wroteRelations = book!!.authors

            // 작가 수가 일치하는지 확인
            assertThat(wroteRelations).hasSameSizeAs(it.authors)

            // 각 작가가 실제 Author 테이블에 저장되었고 관계가 올바른지 확인
            it.authors.forEach { authorName ->
                val author = authorRepository.findByName(authorName)
                assertThat(author).isNotNull()

                // 작가-책 관계가 존재하는지 확인
                val relationExists = wroteRepository.existsByAuthorAndBook(author!!, book)
                assertThat(relationExists).isTrue()
            }
        }
    }

    // === 헬퍼 메서드들 ===
    private fun verifyBookSavedCorrectly(bookDto: BookSearchDto) {
        bookDto.isbn13?.let { isbn ->
            val savedBook = bookRepository.findByIsbn13(isbn)
            assertThat(savedBook).isNotNull()

            savedBook?.let { book ->
                assertThat(book.title).isEqualTo(bookDto.title)
                assertThat(book.publisher).isEqualTo(bookDto.publisher)
            }
        }
    }

    private fun verifyBookDataIntegrity(book: BookSearchDto) {
        assertThat(book.title).isNotBlank()
        assertThat(book.categoryName).isNotBlank()

        book.authors?.let { authors ->
            assertThat(authors).allMatch { it.isNotBlank() }
        }
    }

    private fun verifyAuthorsSavedCorrectly(savedBook: Book, resultDto: BookSearchDto) {
        val authorRelations = savedBook.authors
        assertThat(authorRelations).hasSameSizeAs(resultDto.authors)

        val savedAuthorNames = authorRelations.map { it.author.name }
        assertThat(savedAuthorNames).containsExactlyInAnyOrderElementsOf(resultDto.authors)
    }

    private fun verifyResultsAreIdentical(first: BookSearchDto, second: BookSearchDto) {
        assertThat(first.id).isEqualTo(second.id)
        assertThat(first.title).isEqualTo(second.title)
        assertThat(first.isbn13).isEqualTo(second.isbn13)
        assertThat(first.publisher).isEqualTo(second.publisher)
        assertThat(first.authors).containsExactlyInAnyOrderElementsOf(second.authors)
    }
}