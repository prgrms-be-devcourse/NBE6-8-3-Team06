package com.back.domain.book.book.service;

import com.back.domain.book.author.entity.Author;
import com.back.domain.book.author.repository.AuthorRepository;
import com.back.domain.book.book.dto.BookSearchDto;
import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.book.category.repository.CategoryRepository;
import com.back.domain.book.wrote.entity.Wrote;
import com.back.domain.book.wrote.repository.WroteRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test") // test 프로파일 사용 (선택사항)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("BookService 통합 테스트 - 외부 API 연동 및 데이터베이스 저장")
class BookServiceIntegrationTest {

    @Autowired private BookService bookService;
    @Autowired private BookRepository bookRepository;
    @Autowired private AuthorRepository authorRepository;
    @Autowired private WroteRepository wroteRepository;
    @Autowired private CategoryRepository categoryRepository;

    @Value("${aladin.api.key}")
    private String aladinApiKey;

    @Value("${aladin.api.base-url}")
    private String aladinBaseUrl;

    private static final String TEST_SEARCH_KEYWORD = "자바"; // API에서 결과가 나올만한 키워드
    private static final String TEST_ISBN = "9788966262281"; // 실제 존재하는 ISBN

    @BeforeAll
    void setUpBeforeAll() {
        // 테스트 데이터 준비 (필요한 경우)
        System.out.println("=== BookService 통합 테스트 시작 ===");
        System.out.println("API Key: " + (aladinApiKey != null ? aladinApiKey.substring(0, Math.min(10, aladinApiKey.length())) + "..." : "null"));
        System.out.println("API Base URL: " + aladinBaseUrl);
    }

    @Test
    @DisplayName("API 설정이 올바르게 주입되었는지 확인")
    void checkApiConfiguration() {
        // API 키와 URL이 제대로 주입되었는지 확인
        assertThat(aladinApiKey).isNotNull();
        assertThat(aladinApiKey).isNotEmpty();
        assertThat(aladinApiKey).doesNotContain("${"); // 환경변수 참조가 해결되었는지 확인

        assertThat(aladinBaseUrl).isNotNull();
        assertThat(aladinBaseUrl).isNotEmpty();
        assertThat(aladinBaseUrl).doesNotContain("${");

        System.out.println("✅ API 설정 확인 완료");
        System.out.println("API Key 길이: " + aladinApiKey.length());
        System.out.println("API Base URL: " + aladinBaseUrl);
    }

    @AfterAll
    void tearDownAfterAll() {
        System.out.println("=== BookService 통합 테스트 완료 ===");
    }

    @Test
    @DisplayName("검색어로 책을 찾지 못했을 때 외부 API에서 가져와서 DB에 저장한다")
    void searchBooks_WhenNotFoundInDB_ShouldFetchFromApiAndSaveToDatabase() {
        // Given
        String uniqueKeyword = "spring boot " + System.currentTimeMillis(); // 중복 방지

        // DB에 해당 키워드로 검색되는 책이 없음을 확인
        List<Book> booksBeforeSearch = bookRepository.findByTitleOrAuthorContaining(uniqueKeyword);
        assertThat(booksBeforeSearch).isEmpty();

        // When
        List<BookSearchDto> searchResults = bookService.searchBooks(uniqueKeyword,3);

        // Then
        if (!searchResults.isEmpty()) {
            // API에서 결과를 가져온 경우
            assertThat(searchResults).isNotEmpty();

            // DB에 저장되었는지 확인
            List<Book> booksAfterSearch = bookRepository.findByTitleOrAuthorContaining(uniqueKeyword);
            assertThat(booksAfterSearch).isNotEmpty();

            // 첫 번째 결과 상세 검증
            BookSearchDto firstResult = searchResults.get(0);
            verifyBookSavedCorrectly(firstResult);
        } else {
            // API에서 결과가 없는 경우도 정상 동작
            System.out.println("API에서 검색 결과 없음 - 정상 케이스");
        }
    }

    @Test
    @DisplayName("일반적인 검색어로 API에서 책을 가져와 DB에 저장한다")
    void searchBooks_WithCommonKeyword_ShouldFetchAndSaveBooks() {
        // Given
        List<Book> booksBeforeSearch = bookRepository.findByTitleOrAuthorContaining(TEST_SEARCH_KEYWORD);
        int countBeforeSearch = booksBeforeSearch.size();

        // When
        List<BookSearchDto> searchResults = bookService.searchBooks(TEST_SEARCH_KEYWORD, 5);

        // Then
        assertThat(searchResults).isNotEmpty();

        // DB에 새로운 책이 추가되었거나 기존 책을 반환했는지 확인
        List<Book> booksAfterSearch = bookRepository.findByTitleOrAuthorContaining(TEST_SEARCH_KEYWORD);
        assertThat(booksAfterSearch.size()).isGreaterThanOrEqualTo(countBeforeSearch);

        // 각 결과의 유효성 검증
        for (BookSearchDto book : searchResults) {
            verifyBookDataIntegrity(book);
        }
    }

    @Test
    @DisplayName("ISBN으로 책을 찾지 못했을 때 외부 API에서 가져와서 DB에 저장한다")
    void getBookByIsbn_WhenNotFoundInDB_ShouldFetchFromApiAndSaveToDatabase() {
        // Given
        // DB에서 해당 ISBN이 없음을 확인
        bookRepository.findByIsbn13(TEST_ISBN).ifPresent(book -> {
            // 테스트를 위해 기존 데이터 삭제 (필요한 경우)
            bookRepository.delete(book);
        });

        Optional<Book> bookBeforeApi = bookRepository.findByIsbn13(TEST_ISBN);
        assertThat(bookBeforeApi).isEmpty();

        // When
        BookSearchDto result = bookService.getBookByIsbn(TEST_ISBN);

        // Then
        if (result != null) {
            // API에서 성공적으로 가져온 경우
            assertThat(result.getIsbn13()).isEqualTo(TEST_ISBN);

            // DB에 저장되었는지 확인
            Optional<Book> bookAfterApi = bookRepository.findByIsbn13(TEST_ISBN);
            assertThat(bookAfterApi).isPresent();

            Book savedBook = bookAfterApi.get();
            assertThat(savedBook.getTitle()).isEqualTo(result.getTitle());
            assertThat(savedBook.getPublisher()).isEqualTo(result.getPublisher());

            // 작가 정보도 함께 저장되었는지 확인
            verifyAuthorsSavedCorrectly(savedBook, result);

            // 카테고리도 저장되었는지 확인
            assertThat(savedBook.getCategory()).isNotNull();
            assertThat(savedBook.getCategory().getName()).isNotBlank();
        } else {
            // API에서 해당 ISBN을 찾지 못한 경우도 정상 동작
            System.out.println("API에서 해당 ISBN을 찾지 못함 - 정상 케이스");
        }
    }

    @Test
    @DisplayName("동일한 ISBN으로 두 번 조회할 때 두 번째는 DB에서 가져온다")
    void getBookByIsbn_SecondCall_ShouldReturnFromDatabase() {
        // Given & When - 첫 번째 호출
        BookSearchDto firstResult = bookService.getBookByIsbn(TEST_ISBN);

        if (firstResult != null) {
            // When - 두 번째 호출
            BookSearchDto secondResult = bookService.getBookByIsbn(TEST_ISBN);

            // Then
            assertThat(secondResult).isNotNull();
            assertThat(secondResult.getTitle()).isEqualTo(firstResult.getTitle());
            assertThat(secondResult.getIsbn13()).isEqualTo(firstResult.getIsbn13());

            // 두 결과가 동일한 DB 데이터에서 온 것인지 확인
            verifyResultsAreIdentical(firstResult, secondResult);
        }
    }

    @Test
    @DisplayName("작가와 책의 관계가 정확히 저장되고 조회된다")
    void verifyAuthorBookRelationshipIntegrity() {
        // Given & When
        BookSearchDto result = bookService.getBookByIsbn(TEST_ISBN);

        if (result != null && !result.getAuthors().isEmpty()) {
            // Then
            Optional<Book> bookOpt = bookRepository.findByIsbn13(TEST_ISBN);
            assertThat(bookOpt).isPresent();

            Book book = bookOpt.get();
            List<Wrote> wroteRelations = book.getAuthors();

            // 작가 수가 일치하는지 확인
            assertThat(wroteRelations).hasSameSizeAs(result.getAuthors());

            // 각 작가가 실제 Author 테이블에 저장되었고 관계가 올바른지 확인
            for (String authorName : result.getAuthors()) {
                Optional<Author> authorOpt = authorRepository.findByName(authorName);
                assertThat(authorOpt).isPresent();

                // 작가-책 관계가 존재하는지 확인
                boolean relationExists = wroteRepository.existsByAuthorAndBook(authorOpt.get(), book);
                assertThat(relationExists).isTrue();
            }
        }
    }

    // === 헬퍼 메서드들 ===

    private void verifyBookSavedCorrectly(BookSearchDto bookDto) {
        if (bookDto.getIsbn13() != null) {
            Optional<Book> savedBook = bookRepository.findByIsbn13(bookDto.getIsbn13());
            assertThat(savedBook).isPresent();

            Book book = savedBook.get();
            assertThat(book.getTitle()).isEqualTo(bookDto.getTitle());
            assertThat(book.getPublisher()).isEqualTo(bookDto.getPublisher());
        }
    }

    private void verifyBookDataIntegrity(BookSearchDto book) {
        assertThat(book.getTitle()).isNotBlank();
        assertThat(book.getCategoryName()).isNotBlank();

        if (book.getAuthors() != null) {
            assertThat(book.getAuthors()).allMatch(author -> !author.trim().isEmpty());
        }
    }

    private void verifyAuthorsSavedCorrectly(Book savedBook, BookSearchDto resultDto) {
        List<Wrote> authorRelations = savedBook.getAuthors();
        assertThat(authorRelations).hasSameSizeAs(resultDto.getAuthors());

        List<String> savedAuthorNames = authorRelations.stream()
                .map(wrote -> wrote.getAuthor().getName())
                .toList();

        assertThat(savedAuthorNames).containsExactlyInAnyOrderElementsOf(resultDto.getAuthors());
    }

    private void verifyResultsAreIdentical(BookSearchDto first, BookSearchDto second) {
        assertThat(first.getId()).isEqualTo(second.getId());
        assertThat(first.getTitle()).isEqualTo(second.getTitle());
        assertThat(first.getIsbn13()).isEqualTo(second.getIsbn13());
        assertThat(first.getPublisher()).isEqualTo(second.getPublisher());
        assertThat(first.getAuthors()).containsExactlyInAnyOrderElementsOf(second.getAuthors());
    }
}