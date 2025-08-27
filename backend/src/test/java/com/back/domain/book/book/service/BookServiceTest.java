package com.back.domain.book.book.service;

import com.back.domain.book.author.entity.Author;
import com.back.domain.book.author.repository.AuthorRepository;
import com.back.domain.book.book.dto.BookSearchDto;
import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.book.category.entity.Category;
import com.back.domain.book.category.repository.CategoryRepository;
import com.back.domain.book.client.aladin.AladinApiClient;
import com.back.domain.book.client.aladin.dto.AladinBookDto;
import com.back.domain.book.wrote.entity.Wrote;
import com.back.domain.book.wrote.repository.WroteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private WroteRepository wroteRepository;

    @Mock
    private AladinApiClient aladinApiClient;

    @InjectMocks
    private BookService bookService;

    private Category defaultCategory;
    private Author testAuthor;

    @BeforeEach
    void setUp() {
        defaultCategory = new Category("일반");
        testAuthor = new Author("테스트 작가");
    }

    @Test
    @DisplayName("DB에서 책을 찾을 수 있는 경우 - API 호출하지 않음")
    void searchBooks_WhenBooksFoundInDB_ShouldReturnFromDB() {
        // Given
        String query = "자바";
        Book book = createTestBookWithAuthor();
        when(bookRepository.findByTitleOrAuthorContaining(query))
                .thenReturn(List.of(book));

        // When
        List<BookSearchDto> result = bookService.searchBooks(query, 10);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("테스트 책");
        assertThat(result.get(0).getAuthors()).contains("테스트 작가");

        // API 클라이언트 호출되지 않았는지 확인
        verify(aladinApiClient, never()).searchBooks(anyString(), anyInt());
    }

    @Test
    @DisplayName("DB에 책이 없는 경우 - 알라딘 API 호출")
    void searchBooks_WhenBooksNotFoundInDB_ShouldCallAladinAPI() {
        // Given
        String query = "새로운책";
        AladinBookDto apiBook = createTestAladinBookDto();

        when(bookRepository.findByTitleOrAuthorContaining(query))
                .thenReturn(List.of());
        when(aladinApiClient.searchBooks(query, 10))
                .thenReturn(List.of(apiBook));
        when(categoryRepository.findByName("소설"))
                .thenReturn(Optional.of(new Category("소설")));
        when(authorRepository.findByName("J.K. 롤링"))
                .thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(wroteRepository.existsByAuthorAndBook(any(Author.class), any(Book.class)))
                .thenReturn(false);
        when(wroteRepository.save(any(Wrote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.findByIsbn13(anyString()))
                .thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<BookSearchDto> result = bookService.searchBooks(query, 10);

        // Then
        verify(aladinApiClient).searchBooks(query, 10);
        verify(bookRepository, atLeastOnce()).save(any(Book.class));
        verify(authorRepository, atLeastOnce()).save(any(Author.class));
        verify(wroteRepository, atLeastOnce()).save(any(Wrote.class));
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("ISBN으로 책 조회 - DB에서 찾을 수 있는 경우")
    void getBookByIsbn_WhenBookFoundInDB_ShouldReturnFromDB() {
        // Given
        String isbn = "9788966261024";
        Book book = createTestBookWithAuthor();
        when(bookRepository.findByIsbn13(isbn))
                .thenReturn(Optional.of(book));

        // When
        BookSearchDto result = bookService.getBookByIsbn(isbn);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsbn13()).isEqualTo(isbn);
        assertThat(result.getAuthors()).contains("테스트 작가");

        // API 클라이언트 호출되지 않았는지 확인
        verify(aladinApiClient, never()).getBookByIsbn(anyString());
    }

    @Test
    @DisplayName("ISBN으로 책 조회 - DB에 없는 경우 API 호출")
    void getBookByIsbn_WhenBookNotFoundInDB_ShouldCallAladinAPI() {
        // Given
        String isbn = "9788966261024";
        AladinBookDto apiBook = createTestAladinBookDto();

        when(bookRepository.findByIsbn13(isbn))
                .thenReturn(Optional.empty());
        when(aladinApiClient.getBookByIsbn(isbn))
                .thenReturn(apiBook);
        when(categoryRepository.findByName("소설"))
                .thenReturn(Optional.of(new Category("소설")));
        when(authorRepository.findByName("J.K. 롤링"))
                .thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(wroteRepository.existsByAuthorAndBook(any(Author.class), any(Book.class)))
                .thenReturn(false);
        when(wroteRepository.save(any(Wrote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        BookSearchDto result = bookService.getBookByIsbn(isbn);

        // Then
        verify(aladinApiClient).getBookByIsbn(isbn);
        verify(bookRepository).save(any(Book.class));
        verify(authorRepository).save(any(Author.class));
        verify(wroteRepository).save(any(Wrote.class));
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("상세 정보 보완 - 페이지 수가 없는 경우")
    void enrichMissingDetails_WhenPageMissing_ShouldEnrichFromAPI() {
        // Given
        String query = "페이지없는책";
        AladinBookDto apiBook = AladinBookDto.builder()
                .title("페이지 없는 책")
                .isbn13("9788966261024")
                .totalPage(0) // 페이지 수 없음
                .authors(List.of("테스트 작가"))
                .categoryName("국내도서>소설")
                .mallType("BOOK")
                .build();

        AladinBookDto detailBook = AladinBookDto.builder()
                .title("페이지 없는 책")
                .isbn13("9788966261024")
                .totalPage(300) // 상세 조회에서 페이지 수 있음
                .authors(List.of("테스트 작가"))
                .build();

        when(bookRepository.findByTitleOrAuthorContaining(query))
                .thenReturn(List.of());
        when(aladinApiClient.searchBooks(query, 10))
                .thenReturn(List.of(apiBook));
        when(aladinApiClient.getBookDetails("9788966261024"))
                .thenReturn(detailBook);
        when(categoryRepository.findByName("소설"))
                .thenReturn(Optional.of(new Category("소설")));
        when(authorRepository.findByName("테스트 작가"))
                .thenReturn(Optional.of(testAuthor));
        when(wroteRepository.existsByAuthorAndBook(any(Author.class), any(Book.class)))
                .thenReturn(false);
        when(wroteRepository.save(any(Wrote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.findByIsbn13("9788966261024"))
                .thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<BookSearchDto> result = bookService.searchBooks(query, 10);

        // Then
        verify(aladinApiClient).getBookDetails("9788966261024");
        verify(bookRepository, atLeastOnce()).save(argThat(book ->
                book.getTotalPage() == 300
        ));
    }

    @Test
    @DisplayName("중복된 작가 정보 처리 - 이미 존재하는 작가는 새로 생성하지 않음")
    void saveAuthors_WhenAuthorAlreadyExists_ShouldNotCreateDuplicate() {
        // Given
        String query = "기존작가책";
        AladinBookDto apiBook = createTestAladinBookDto();

        when(bookRepository.findByTitleOrAuthorContaining(query))
                .thenReturn(List.of());
        when(aladinApiClient.searchBooks(query, 10))
                .thenReturn(List.of(apiBook));
        when(categoryRepository.findByName("소설"))
                .thenReturn(Optional.of(new Category("소설")));
        when(authorRepository.findByName("J.K. 롤링"))
                .thenReturn(Optional.of(testAuthor)); // 이미 존재하는 작가
        when(wroteRepository.existsByAuthorAndBook(any(Author.class), any(Book.class)))
                .thenReturn(false);
        when(wroteRepository.save(any(Wrote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.findByIsbn13(anyString()))
                .thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        bookService.searchBooks(query, 10);

        // Then
        verify(authorRepository, never()).save(any(Author.class)); // 새로운 작가 생성 안 함
        verify(wroteRepository, atLeastOnce()).save(any(Wrote.class)); // 관계는 생성
    }

    @Test
    @DisplayName("알라딘 API 호출 실패 시 빈 리스트 반환")
    void searchBooks_WhenAPICallFails_ShouldReturnEmptyList() {
        // Given
        String query = "실패테스트";
        when(bookRepository.findByTitleOrAuthorContaining(query))
                .thenReturn(List.of());
        when(aladinApiClient.searchBooks(query, 10))
                .thenReturn(List.of()); // API 클라이언트에서 빈 리스트 반환

        // When
        List<BookSearchDto> result = bookService.searchBooks(query, 10);

        // Then
        assertThat(result).isEmpty();
        verify(authorRepository, never()).save(any(Author.class));
        verify(wroteRepository, never()).save(any(Wrote.class));
    }

    @Test
    @DisplayName("카테고리 경로에서 2번째 깊이 추출 - 소설")
    void categoryExtraction_ShouldExtractSecondLevel_Novel() {
        // Given
        String query = "소설책";
        AladinBookDto apiBook = AladinBookDto.builder()
                .title("Test Novel")
                .isbn13("9788966261024")
                .categoryName("국내도서>소설>한국소설>현대소설")
                .mallType("BOOK")
                .authors(List.of("테스트 작가"))
                .build();

        when(bookRepository.findByTitleOrAuthorContaining(query))
                .thenReturn(List.of());
        when(aladinApiClient.searchBooks(query, 10))
                .thenReturn(List.of(apiBook));
        when(categoryRepository.findByName("소설"))
                .thenReturn(Optional.of(new Category("소설")));
        when(authorRepository.findByName("테스트 작가"))
                .thenReturn(Optional.of(testAuthor));
        when(wroteRepository.existsByAuthorAndBook(any(Author.class), any(Book.class)))
                .thenReturn(false);
        when(wroteRepository.save(any(Wrote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.findByIsbn13(anyString()))
                .thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        bookService.searchBooks(query, 10);

        // Then
        verify(categoryRepository, atLeastOnce()).findByName("소설");
        verify(bookRepository, atLeastOnce()).save(any(Book.class));
    }

    @Test
    @DisplayName("새로운 카테고리 자동 생성")
    void categoryExtraction_ShouldCreateNewCategory() {
        // Given
        String query = "새분야책";
        AladinBookDto apiBook = AladinBookDto.builder()
                .title("Test Book")
                .isbn13("9788966261024")
                .categoryName("국내도서>새로운분야>세부분야")
                .mallType("BOOK")
                .authors(List.of("테스트 작가"))
                .build();

        when(bookRepository.findByTitleOrAuthorContaining(query))
                .thenReturn(List.of());
        when(aladinApiClient.searchBooks(query, 10))
                .thenReturn(List.of(apiBook));
        when(categoryRepository.findByName("새로운분야"))
                .thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(authorRepository.findByName("테스트 작가"))
                .thenReturn(Optional.of(testAuthor));
        when(wroteRepository.existsByAuthorAndBook(any(Author.class), any(Book.class)))
                .thenReturn(false);
        when(wroteRepository.save(any(Wrote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.findByIsbn13(anyString()))
                .thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        bookService.searchBooks(query, 10);

        // Then
        verify(categoryRepository, atLeastOnce()).findByName("새로운분야");
        verify(categoryRepository, atLeastOnce()).save(argThat(category ->
                "새로운분야".equals(category.getName())
        ));
        verify(bookRepository, atLeastOnce()).save(any(Book.class));
    }

    @Test
    @DisplayName("mallType 기반 기본 카테고리 - FOREIGN")
    void categoryExtraction_ForeignBook_ShouldUseForeignCategory() {
        // Given
        String query = "외국책";
        AladinBookDto apiBook = AladinBookDto.builder()
                .title("Foreign Book")
                .isbn13("9788966261024")
                .categoryName(null) // 카테고리 정보 없음
                .mallType("FOREIGN")
                .authors(List.of("테스트 작가"))
                .build();

        when(bookRepository.findByTitleOrAuthorContaining(query))
                .thenReturn(List.of());
        when(aladinApiClient.searchBooks(query, 10))
                .thenReturn(List.of(apiBook));
        when(categoryRepository.findByName("외국도서"))
                .thenReturn(Optional.of(new Category("외국도서")));
        when(authorRepository.findByName("테스트 작가"))
                .thenReturn(Optional.of(testAuthor));
        when(wroteRepository.existsByAuthorAndBook(any(Author.class), any(Book.class)))
                .thenReturn(false);
        when(wroteRepository.save(any(Wrote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.findByIsbn13(anyString()))
                .thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        bookService.searchBooks(query, 10);

        // Then
        verify(categoryRepository, atLeastOnce()).findByName("외국도서");
        verify(bookRepository, atLeastOnce()).save(any(Book.class));
    }

    // ===== Helper Methods =====

    private Book createTestBookWithAuthor() {
        Book book = new Book();
        book.setTitle("테스트 책");
        book.setImageUrl("http://test.com/image.jpg");
        book.setPublisher("테스트 출판사");
        book.setIsbn13("9788966261024");
        book.setTotalPage(300);
        book.setAvgRate(4.5f);
        book.setCategory(defaultCategory);

        // 작가 관계 설정
        Wrote wrote = new Wrote(testAuthor, book);
        book.getAuthors().add(wrote);

        return book;
    }

    private AladinBookDto createTestAladinBookDto() {
        return AladinBookDto.builder()
                .title("해리 포터와 마법사의 돌")
                .imageUrl("http://image.aladin.co.kr/test.jpg")
                .publisher("문학수첩")
                .isbn13("9788966261024")
                .totalPage(250)
                .publishedDate(LocalDateTime.of(2024, 1, 15, 0, 0))
                .categoryName("국내도서>소설>판타지소설")
                .mallType("BOOK")
                .authors(List.of("J.K. 롤링"))
                .build();
    }
}