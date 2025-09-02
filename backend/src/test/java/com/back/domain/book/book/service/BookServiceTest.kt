package com.back.domain.book.book.service

import com.back.domain.book.author.entity.Author
import com.back.domain.book.author.repository.AuthorRepository
import com.back.domain.book.book.dto.BookSearchDto
import com.back.domain.book.book.entity.Book
import com.back.domain.book.book.repository.BookRepository
import com.back.domain.book.category.entity.Category
import com.back.domain.book.category.repository.CategoryRepository
import com.back.domain.book.client.aladin.AladinApiClient
import com.back.domain.book.client.aladin.dto.AladinBookDto
import com.back.domain.book.wrote.entity.Wrote
import com.back.domain.book.wrote.repository.WroteRepository
import com.back.domain.bookmarks.repository.BookmarkRepository
import com.back.domain.review.review.repository.ReviewRepository
import com.back.domain.review.reviewRecommend.service.ReviewRecommendService
import com.back.domain.review.review.service.ReviewDtoService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mock
import org.mockito.InjectMocks
import org.mockito.invocation.InvocationOnMock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.mockito.stubbing.Answer
import org.mockito.Mockito.lenient
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
internal class BookServiceTest {
    @Mock
    private lateinit var bookRepository: BookRepository

    @Mock
    private lateinit var categoryRepository: CategoryRepository

    @Mock
    private lateinit var authorRepository: AuthorRepository

    @Mock
    private lateinit var wroteRepository: WroteRepository

    @Mock
    private lateinit var aladinApiClient: AladinApiClient

    @Mock
    private lateinit var bookmarkRepository: BookmarkRepository

    @Mock
    private lateinit var reviewRepository: ReviewRepository

    @Mock
    private lateinit var reviewRecommendService: ReviewRecommendService

    @Mock
    private lateinit var reviewDtoService: ReviewDtoService

    @InjectMocks
    private lateinit var bookService: BookService

    private lateinit var defaultCategory: Category
    private lateinit var testAuthor: Author

    @BeforeEach
    fun setUp() {
        defaultCategory = Category("일반")
        testAuthor = Author("테스트 작가")
    }

    @Test
    @DisplayName("DB에서 책을 찾을 수 있는 경우 - API 호출하지 않음")
    fun searchBooks_WhenBooksFoundInDB_ShouldReturnFromDB() {
        // Given
        val query = "테스트 책"
        val book = createTestBookWithAuthor()
        Mockito.`when`(bookRepository.findValidBooksByTitleOrAuthorContaining(query))
            .thenReturn(mutableListOf<Book>(book))

        // When
        val result = bookService.searchBooks(query, 10, null)

        // Then
        Assertions.assertThat<BookSearchDto?>(result).hasSize(1)
        Assertions.assertThat(result[0].title).isEqualTo("테스트 책")
        Assertions.assertThat<String>(result[0].authors).contains("테스트 작가")

        // API 클라이언트 호출되지 않았는지 확인
        Mockito.verify(aladinApiClient, Mockito.never())
            .searchBooks(any(), any())
    }

    @Test
    @DisplayName("DB에 책이 없는 경우 - 알라딘 API 호출")
    fun searchBooks_WhenBooksNotFoundInDB_ShouldCallAladinAPI() {
        // Given
        val query = "새로운책"
        val apiBook = createTestAladinBookDto()

        Mockito.`when`(bookRepository.findValidBooksByTitleOrAuthorContaining(query))
            .thenReturn(mutableListOf<Book>())
        Mockito.`when`(aladinApiClient.searchBooks(query, 10))
            .thenReturn(mutableListOf<AladinBookDto>(apiBook))
        Mockito.`when`(categoryRepository.findByName("소설"))
            .thenReturn(Category("소설"))
        Mockito.`when`(authorRepository.findByName("J.K. 롤링"))
            .thenReturn(null)
        Mockito.`when`(authorRepository.save<Author?>(any()))
            .thenAnswer(Answer { invocation: InvocationOnMock -> invocation.getArgument<Any?>(0) })
        // wroteRepository.existsByAuthorAndBook을 완전히 우회
        Mockito.doAnswer { invocation ->
            // 어떤 파라미터가 와도 항상 false 반환
            false
        }.`when`(wroteRepository).existsByAuthorAndBook(any(), any())
        Mockito.`when`(wroteRepository.save<Wrote?>(any()))
            .thenAnswer(Answer { invocation: InvocationOnMock -> invocation.getArgument<Any?>(0) })
        Mockito.`when`(bookRepository.findByIsbn13(any()))
            .thenReturn(null)
        Mockito.`when`(bookRepository.save<Book?>(any()))
            .thenAnswer(Answer { invocation: InvocationOnMock -> invocation.getArgument<Any?>(0) })

        // When
        val result = bookService.searchBooks(query, 10, null)

        // Then
        Mockito.verify(aladinApiClient).searchBooks(query, 10)
        Mockito.verify(bookRepository, Mockito.atLeastOnce()).save<Book?>(any())
        Mockito.verify(authorRepository, Mockito.atLeastOnce()).save<Author?>(any())
        Mockito.verify(wroteRepository, Mockito.atLeastOnce()).save<Wrote?>(any())
        Assertions.assertThat<BookSearchDto?>(result).hasSize(1)
    }

    @Test
    @DisplayName("ISBN으로 책 조회 - DB에서 찾을 수 있는 경우")
    fun getBookByIsbn_WhenBookFoundInDB_ShouldReturnFromDB() {
        // Given
        val isbn = "9788966261024"
        val book = createTestBookWithAuthor()
        Mockito.`when`(bookRepository.findByIsbn13(isbn))
            .thenReturn(book)

        // When
        val result = bookService.getBookByIsbn(isbn, null)

        // Then
        Assertions.assertThat<BookSearchDto?>(result).isNotNull()
        Assertions.assertThat(result?.isbn13).isEqualTo(isbn)
        Assertions.assertThat<String>(result?.authors).contains("테스트 작가")

        // API 클라이언트 호출되지 않았는지 확인
        Mockito.verify(aladinApiClient, Mockito.never()).getBookByIsbn(any())
    }

    @Test
    @DisplayName("ISBN으로 책 조회 - DB에 없는 경우 API 호출")
    fun getBookByIsbn_WhenBookNotFoundInDB_ShouldCallAladinAPI() {
        // Given
        val isbn = "9788966261024"
        val apiBook = createTestAladinBookDto()

        Mockito.`when`(bookRepository.findByIsbn13(isbn))
            .thenReturn(null)
        Mockito.`when`(aladinApiClient.getBookByIsbn(isbn))
            .thenReturn(apiBook)
        Mockito.`when`(categoryRepository.findByName("소설"))
            .thenReturn(Category("소설"))
        Mockito.`when`(authorRepository.findByName("J.K. 롤링"))
            .thenReturn(null)
        Mockito.`when`(authorRepository.save<Author?>(any()))
            .thenAnswer(Answer { invocation: InvocationOnMock -> invocation.getArgument<Any?>(0) })
        // wroteRepository.existsByAuthorAndBook을 완전히 우회
        Mockito.doAnswer { invocation ->
            // 어떤 파라미터가 와도 항상 false 반환
            false
        }.`when`(wroteRepository).existsByAuthorAndBook(any(), any())
        Mockito.`when`(wroteRepository.save<Wrote?>(any()))
            .thenAnswer(Answer { invocation: InvocationOnMock -> invocation.getArgument<Any?>(0) })
        Mockito.`when`(bookRepository.save<Book?>(any()))
            .thenAnswer(Answer { invocation: InvocationOnMock -> invocation.getArgument<Any?>(0) })

        // When
        val result = bookService.getBookByIsbn(isbn, null)

        // Then
        Mockito.verify(aladinApiClient).getBookByIsbn(isbn)
        Mockito.verify(bookRepository).save<Book?>(any())
        Mockito.verify(authorRepository)
            .save<Author?>(any())
        Mockito.verify(wroteRepository).save<Wrote?>(any())
        Assertions.assertThat<BookSearchDto?>(result).isNotNull()
    }

    @Test
    @DisplayName("상세 정보 보완 - 페이지 수가 없는 경우")
    fun enrichMissingDetails_WhenPageMissing_ShouldEnrichFromAPI() {
        // Given
        val query = "페이지없는책"
        val apiBook = AladinBookDto(
            "페이지 없는 책",  // title
            null,  // imageUrl
            null,  // publisher
            "9788966261024",  // isbn13
            0,  // totalPage - 페이지 수 없음
            null,  // publishedDate
            "국내도서>소설",  // categoryName
            "BOOK",  // mallType
            mutableListOf<String>("테스트 작가") // authors
        )

        val detailBook = AladinBookDto(
            "페이지 없는 책",  // title
            null,  // imageUrl
            null,  // publisher
            "9788966261024",  // isbn13
            300,  // totalPage - 상세 조회에서 페이지 수 있음
            null,  // publishedDate
            null,  // categoryName
            null,  // mallType
            mutableListOf<String>("테스트 작가") // authors
        )

        Mockito.`when`(bookRepository.findValidBooksByTitleOrAuthorContaining(query))
            .thenReturn(mutableListOf<Book>())
        Mockito.`when`(aladinApiClient.searchBooks(query, 10))
            .thenReturn(mutableListOf<AladinBookDto>(apiBook))
        Mockito.`when`(aladinApiClient.getBookDetails("9788966261024"))
            .thenReturn(detailBook)
        Mockito.`when`(categoryRepository.findByName("소설"))
            .thenReturn(Category("소설"))
        Mockito.`when`(authorRepository.findByName("테스트 작가"))
            .thenReturn(testAuthor)
        // wroteRepository.existsByAuthorAndBook을 완전히 우회
        Mockito.doAnswer { invocation ->
            // 어떤 파라미터가 와도 항상 false 반환
            false
        }.`when`(wroteRepository).existsByAuthorAndBook(any(), any())
        Mockito.`when`(wroteRepository.save<Wrote?>(any()))
            .thenAnswer(Answer { invocation: InvocationOnMock -> invocation.getArgument<Any?>(0) })
        Mockito.`when`(bookRepository.findByIsbn13("9788966261024"))
            .thenReturn(null)
        Mockito.`when`(bookRepository.save<Book?>(any()))
            .thenAnswer(Answer { invocation: InvocationOnMock -> invocation.getArgument<Any?>(0) })

        // When
        val result = bookService.searchBooks(query, 10, null)

        // Then
        Mockito.verify(aladinApiClient).getBookDetails("9788966261024")
        Mockito.verify<BookRepository?>(bookRepository, Mockito.atLeastOnce())
            .save<Book>(argThat<Book> { book -> book.totalPage == 300 })
    }

    @Test
    @DisplayName("중복된 작가 정보 처리 - 이미 존재하는 작가는 새로 생성하지 않음")
    fun saveAuthors_WhenAuthorAlreadyExists_ShouldNotCreateDuplicate() {
        // Given
        val query = "기존작가책"
        val apiBook = createTestAladinBookDto()

        Mockito.`when`(bookRepository.findValidBooksByTitleOrAuthorContaining(query))
            .thenReturn(mutableListOf<Book>())
        Mockito.`when`(aladinApiClient.searchBooks(query, 10))
            .thenReturn(mutableListOf<AladinBookDto>(apiBook))
        Mockito.`when`(categoryRepository.findByName("소설"))
            .thenReturn(Category("소설"))
        Mockito.`when`(authorRepository.findByName("J.K. 롤링"))
            .thenReturn(testAuthor) // 이미 존재하는 작가
        // wroteRepository.existsByAuthorAndBook을 완전히 우회
        Mockito.doAnswer { invocation ->
            // 어떤 파라미터가 와도 항상 false 반환
            false
        }.`when`(wroteRepository).existsByAuthorAndBook(any(), any())
        Mockito.`when`(wroteRepository.save<Wrote?>(any()))
            .thenAnswer(Answer { invocation: InvocationOnMock -> invocation.getArgument<Any?>(0) })
        Mockito.`when`(bookRepository.findByIsbn13(any()))
            .thenReturn(null)
        Mockito.`when`(bookRepository.save<Book?>(any()))
            .thenAnswer(Answer { invocation: InvocationOnMock -> invocation.getArgument<Any?>(0) })

        // When
        bookService.searchBooks(query, 10, null)

        // Then
        Mockito.verify(authorRepository, Mockito.never())
            .save<Author?>(any()) // 새로운 작가 생성 안 함
        Mockito.verify(wroteRepository, Mockito.atLeastOnce()).save<Wrote?>(any()) // 관계는 생성
    }

    @Test
    @DisplayName("알라딘 API 호출 실패 시 빈 리스트 반환")
    fun searchBooks_WhenAPICallFails_ShouldReturnEmptyList() {
        // Given
        val query = "실패테스트"
        Mockito.`when`(bookRepository.findValidBooksByTitleOrAuthorContaining(query))
            .thenReturn(mutableListOf<Book>())
        Mockito.`when`(aladinApiClient.searchBooks(query, 10))
            .thenReturn(mutableListOf<AladinBookDto>()) // API 클라이언트에서 빈 리스트 반환

        // When
        val result = bookService.searchBooks(query, 10, null)

        // Then
        Assertions.assertThat<BookSearchDto?>(result).isEmpty()
        Mockito.verify(authorRepository, Mockito.never())
            .save<Author?>(any())
        Mockito.verify(wroteRepository, Mockito.never())
            .save<Wrote?>(any())
    }

    @Test
    @DisplayName("카테고리 경로에서 2번째 깊이 추출 - 소설")
    fun categoryExtraction_ShouldExtractSecondLevel_Novel() {
        // Given
        val query = "소설책"
        val apiBook = AladinBookDto(
            "Test Novel",  // title
            null,  // imageUrl
            null,  // publisher
            "9788966261024",  // isbn13
            300,  // totalPage
            null,  // publishedDate
            "국내도서>소설>한국소설>현대소설",  // categoryName
            "BOOK",  // mallType
            mutableListOf<String>("테스트 작가") // authors
        )

        Mockito.`when`(bookRepository.findValidBooksByTitleOrAuthorContaining(query))
            .thenReturn(mutableListOf<Book>())
        Mockito.`when`(aladinApiClient.searchBooks(query, 10))
            .thenReturn(mutableListOf<AladinBookDto>(apiBook))
        Mockito.`when`(categoryRepository.findByName("소설"))
            .thenReturn(Category("소설"))
        Mockito.`when`(authorRepository.findByName("테스트 작가"))
            .thenReturn(testAuthor)
        // wroteRepository.existsByAuthorAndBook을 완전히 우회
        Mockito.doAnswer { invocation ->
            // 어떤 파라미터가 와도 항상 false 반환
            false
        }.`when`(wroteRepository).existsByAuthorAndBook(any(), any())
        Mockito.`when`(wroteRepository.save<Wrote?>(any()))
            .thenAnswer(Answer { invocation: InvocationOnMock -> invocation.getArgument<Any?>(0) })
        Mockito.`when`(bookRepository.findByIsbn13(any()))
            .thenReturn(null)
        Mockito.`when`(bookRepository.save<Book?>(any()))
            .thenAnswer(Answer { invocation: InvocationOnMock -> invocation.getArgument<Any?>(0) })

        // When
        bookService.searchBooks(query, 10, null)

        // Then
        Mockito.verify(categoryRepository, Mockito.atLeastOnce()).findByName("소설")
        Mockito.verify(bookRepository, Mockito.atLeastOnce()).save<Book?>(any())
    }

    @Test
    @DisplayName("새로운 카테고리 자동 생성")
    fun categoryExtraction_ShouldCreateNewCategory() {
        // Given
        val query = "새분야책"
        val apiBook = AladinBookDto(
            "Test Book",  // title
            null,  // imageUrl
            null,  // publisher
            "9788966261024",  // isbn13
            300,  // totalPage
            null,  // publishedDate
            "국내도서>새로운분야>세부분야",  // categoryName
            "BOOK",  // mallType
            mutableListOf<String>("테스트 작가") // authors
        )

        Mockito.`when`(bookRepository.findValidBooksByTitleOrAuthorContaining(query))
            .thenReturn(mutableListOf<Book>())
        Mockito.`when`(aladinApiClient.searchBooks(query, 10))
            .thenReturn(mutableListOf<AladinBookDto>(apiBook))
        Mockito.`when`(categoryRepository.findByName("새로운분야"))
            .thenReturn(null)
        Mockito.`when`(categoryRepository.save<Category?>(any()))
            .thenAnswer(Answer { invocation: InvocationOnMock -> invocation.getArgument<Any?>(0) })
        Mockito.`when`(authorRepository.findByName("테스트 작가"))
            .thenReturn(testAuthor)
        // wroteRepository.existsByAuthorAndBook을 완전히 우회
        Mockito.doAnswer { invocation ->
            // 어떤 파라미터가 와도 항상 false 반환
            false
        }.`when`(wroteRepository).existsByAuthorAndBook(any(), any())
        Mockito.`when`(wroteRepository.save<Wrote?>(any()))
            .thenAnswer(Answer { invocation: InvocationOnMock -> invocation.getArgument<Any?>(0) })
        Mockito.`when`(bookRepository.findByIsbn13(any()))
            .thenReturn(null)
        Mockito.`when`(bookRepository.save<Book?>(any()))
            .thenAnswer(Answer { invocation: InvocationOnMock -> invocation.getArgument<Any?>(0) })

        // When
        bookService.searchBooks(query, 10, null)

        // Then
        Mockito.verify(categoryRepository, Mockito.atLeastOnce()).findByName("새로운분야")
        Mockito.verify(categoryRepository, Mockito.atLeastOnce())
            .save<Category>(argThat<Category> { category -> "새로운분야" == category.name })
        Mockito.verify(bookRepository, Mockito.atLeastOnce()).save<Book?>(any())
    }

    @Test
    @DisplayName("mallType 기반 기본 카테고리 - FOREIGN")
    fun categoryExtraction_ForeignBook_ShouldUseForeignCategory() {
        // Given
        val query = "외국책"
        val apiBook = AladinBookDto(
            "Foreign Book",  // title
            null,  // imageUrl
            null,  // publisher
            "9788966261024",  // isbn13
            250,  // totalPage - 페이지 수 추가하여 상세 조회 불필요하게 만듦
            null,  // publishedDate
            null,  // categoryName - 카테고리 정보 없음
            "FOREIGN",  // mallType
            mutableListOf<String>("테스트 작가") // authors
        )

        Mockito.`when`(bookRepository.findValidBooksByTitleOrAuthorContaining(query))
            .thenReturn(mutableListOf<Book>())
        Mockito.`when`(aladinApiClient.searchBooks(query, 10))
            .thenReturn(mutableListOf<AladinBookDto>(apiBook))
        Mockito.`when`(categoryRepository.findByName("외국도서"))
            .thenReturn(Category("외국도서"))
        Mockito.`when`(authorRepository.findByName("테스트 작가"))
            .thenReturn(testAuthor)
        // wroteRepository.existsByAuthorAndBook을 완전히 우회
        Mockito.doAnswer { invocation ->
            // 어떤 파라미터가 와도 항상 false 반환
            false
        }.`when`(wroteRepository).existsByAuthorAndBook(any(), any())
        Mockito.`when`(wroteRepository.save<Wrote?>(any()))
            .thenAnswer(Answer { invocation: InvocationOnMock -> invocation.getArgument<Any?>(0) })
        Mockito.`when`(bookRepository.findByIsbn13(any()))
            .thenReturn(null)
        Mockito.`when`(bookRepository.save<Book?>(any()))
            .thenAnswer(Answer { invocation: InvocationOnMock -> invocation.getArgument<Any?>(0) })

        // When
        bookService.searchBooks(query, 10, null)

        // Then
        Mockito.verify(categoryRepository, Mockito.atLeastOnce()).findByName("외국도서")
        Mockito.verify(bookRepository, Mockito.atLeastOnce()).save<Book?>(any())
    }

    // ===== Helper Methods =====
    private fun createTestBookWithAuthor(): Book {
        val book = Book("테스트 책", "테스트 출판사", defaultCategory)
        book.imageUrl = "http://test.com/image.jpg"
        book.isbn13 = "9788966261024"
        book.totalPage = 300
        book.avgRate = 4.5f

        // 작가 관계 설정
        val wrote = Wrote(testAuthor, book)
        book.addAuthor(wrote)

        return book
    }

    private fun createTestAladinBookDto(): AladinBookDto {
        return AladinBookDto(
            "해리 포터와 마법사의 돌",  // title
            "http://image.aladin.co.kr/test.jpg",  // imageUrl
            "문학수첩",  // publisher
            "9788966261024",  // isbn13
            250,  // totalPage
            LocalDateTime.of(2024, 1, 15, 0, 0),  // publishedDate
            "국내도서>소설>판타지소설",  // categoryName
            "BOOK",  // mallType
            mutableListOf<String>("J.K. 롤링") // authors
        )
    }
}