package com.back.domain.book.book.service;

import com.back.domain.book.author.entity.Author;
import com.back.domain.book.author.repository.AuthorRepository;
import com.back.domain.book.book.dto.BookDetailDto;
import com.back.domain.book.book.dto.BookSearchDto;
import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.book.category.entity.Category;
import com.back.domain.book.category.repository.CategoryRepository;
import com.back.domain.book.client.aladin.AladinApiClient;
import com.back.domain.book.client.aladin.dto.AladinBookDto;
import com.back.domain.book.wrote.entity.Wrote;
import com.back.domain.book.wrote.repository.WroteRepository;
import com.back.domain.bookmarks.constant.ReadState;
import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.bookmarks.repository.BookmarkRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.review.review.dto.ReviewResponseDto;
import com.back.domain.review.review.entity.Review;
import com.back.domain.review.review.repository.ReviewRepository;
import com.back.domain.review.reviewRecommend.service.ReviewRecommendService;
import com.back.global.dto.PageResponseDto;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final WroteRepository wroteRepository;
    private final AladinApiClient aladinApiClient;
    private final BookmarkRepository bookmarkRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewRecommendService reviewRecommendService;

    /**
     * 페이징 지원 검색 메서드 - DB에서 직접 정렬 처리
     */
    @Transactional
    public Page<BookSearchDto> searchBooks(String query, Pageable pageable, Member member) {
        // 1. DB에서 페이징과 정렬이 적용된 검색 시도
        Page<Book> dbResults = bookRepository.findValidBooksByTitleOrAuthorContainingWithPaging(query, pageable);

        if (!dbResults.isEmpty()) {
            log.info("DB에서 찾은 유효한 책: {} 권 (전체: {})",
                    dbResults.getNumberOfElements(), dbResults.getTotalElements());
            return dbResults.map(book -> convertToDto(book, member));
        }

        log.info("DB에 유효한 책이 없어서 알라딘 API에서 검색: {}", query);

        // 2. API에서 검색하여 DB에 저장 (충분한 양)
        List<AladinBookDto> apiBooks = aladinApiClient.searchBooks(query, 100);

        // 3. API 결과를 엔티티로 변환하고 저장
        List<Book> savedBooks = apiBooks.stream()
                .map(this::convertAndSaveBook)
                .filter(book -> book != null)
                .collect(Collectors.toList());

        // 4. 상세 정보 보완
        enrichMissingDetails(savedBooks);

        // 5. DB에서 다시 페이징과 정렬이 적용된 검색
        log.info("API 검색 및 저장 완료. DB에서 다시 검색하여 페이징 처리");
        Page<Book> finalResults = bookRepository.findValidBooksByTitleOrAuthorContainingWithPaging(query, pageable);

        if (finalResults.isEmpty()) {
            log.warn("API 검색 후에도 유효한 책이 없음: {}", query);
            return Page.empty(pageable);
        }

        return finalResults.map(book -> convertToDto(book, member));
    }

    /**
     * 기존 limit 방식 검색 메서드 (하위 호환성 유지)
     */
    @Transactional
    public List<BookSearchDto> searchBooks(String query, int limit, Member member) {
        // 1. DB에서 유효한 책들만 먼저 확인
        List<Book> validBooksFromDb = bookRepository.findValidBooksByTitleOrAuthorContaining(query);
        if (!validBooksFromDb.isEmpty()) {
            log.info("DB에서 찾은 유효한 책: {} 권", validBooksFromDb.size());
            return convertToDto(validBooksFromDb.stream().limit(limit).toList(), member);
        }

        log.info("DB에 유효한 책이 없어서 알라딘 API에서 검색: {}", query);

        // 2. API에서 검색
        List<AladinBookDto> apiBooks = aladinApiClient.searchBooks(query, limit);

        // 3. API 결과를 엔티티로 변환하고 저장
        List<Book> savedBooks = apiBooks.stream()
                .map(this::convertAndSaveBook)
                .filter(book -> book != null)
                .collect(Collectors.toList());

        // 4. 상세 정보 보완
        savedBooks = enrichMissingDetails(savedBooks);

        return convertToDto(savedBooks, member);
    }

    /**
     * 기존 limit 방식 검색 메서드 (Member 없는 버전) - 하위 호환성 유지
     */
    @Transactional
    public List<BookSearchDto> searchBooks(String query, int limit) {
        return searchBooks(query, limit, null);
    }


    /**
     * ISBN으로 책 조회 - DB에 없으면 API에서 가져와서 저장 (Member 정보 포함)
     */
    @Transactional
    public BookSearchDto getBookByIsbn(String isbn, Member member) {
        // 먼저 유효한 책(페이지 수 > 0)이 있는지 확인
        Optional<Book> validBookFromDb = bookRepository.findValidBookByIsbn13(isbn);

        if (validBookFromDb.isPresent()) {
            log.info("DB에서 유효한 책 발견: {}", validBookFromDb.get().getTitle());
            return convertToDto(validBookFromDb.get(), member);
        }

        // 유효한 책이 없다면 기존 책(페이지 수 0 포함)이 있는지 확인
        Optional<Book> existingBookFromDb = bookRepository.findByIsbn13(isbn);

        if (existingBookFromDb.isPresent()) {
            Book existingBook = existingBookFromDb.get();
            log.info("DB에 페이지 수 0인 책이 있어서 상세 정보 보완 시도: {}", existingBook.getTitle());

            // 상세 정보 보완 시도
            enrichBookWithDetailInfo(existingBook);

            // 보완 후에도 페이지 수가 0이면 null 반환
            if (existingBook.getTotalPage() == 0) {
                log.warn("상세 정보 보완 후에도 페이지 수가 0인 책: {}", existingBook.getTitle());
                return null;
            }

            return convertToDto(existingBook, member);
        }

        // API에서 검색
        log.info("DB에 없어서 API에서 검색: {}", isbn);
        AladinBookDto apiBook = aladinApiClient.getBookByIsbn(isbn);
        if (apiBook == null) {
            return null;
        }

        Book savedBook = convertAndSaveBook(apiBook);
        if (savedBook == null) {
            return null;
        }

        // 상세 정보 보완
        enrichBookWithDetailInfo(savedBook);

        // 페이지 수가 0이면 null 반환
        if (savedBook.getTotalPage() == 0) {
            log.warn("새로 저장한 책의 페이지 수가 0: {}", savedBook.getTitle());
            return null;
        }

        return convertToDto(savedBook, member);
    }

    /**
     * 기존 getBookByIsbn 메서드 (Member 없는 버전) - 하위 호환성 유지
     */
    @Transactional
    public BookSearchDto getBookByIsbn(String isbn) {
        return getBookByIsbn(isbn, null);
    }

    /**
     * 전체 책 조회 (페이징, Member 정보 포함) - DB에서 직접 정렬 처리
     */
    public Page<BookSearchDto> getAllBooks(Pageable pageable, Member member) {
        log.info("전체 유효한 책 조회: page={}, size={}, sort={}, member={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(),
                member != null ? member.getId() : "null");

        try {
            // DB에서 페이징과 정렬이 적용된 조회 (페이지 수가 0보다 큰 유효한 책들만)
            Page<Book> validBookPage = bookRepository.findAllValidBooks(pageable);

            return validBookPage.map(book -> {
                BookSearchDto dto = convertToDto(book, member);
                log.debug("Book {} converted with readState: {}", book.getId(), dto.getReadState());
                return dto;
            });
        } catch (Exception e) {
            log.error("전체 책 조회 중 오류 발생: {}", e.getMessage());
            throw new ServiceException("500-1", "전체 책 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 기존 getAllBooks 메서드 (Member 없는 버전) - 하위 호환성 유지
     */
    public Page<BookSearchDto> getAllBooks(Pageable pageable) {
        return getAllBooks(pageable, null);
    }

    /**
     * ID로 책 상세 정보 조회 (리뷰 페이징 포함)
     */
    @Transactional(readOnly = true)
    public BookDetailDto getBookDetailById(int id, Pageable pageable, Member member) {
        log.info("책 상세 조회: id={}, page={}, size={}, member={}",
                id, pageable.getPageNumber(), pageable.getPageSize(),
                member != null ? member.getId() : "null");

        try {
            // 책 조회 (페이지 수 0인 책도 상세 조회는 가능)
            Optional<Book> bookOptional = bookRepository.findById(id);
            if (bookOptional.isEmpty()) {
                return null;
            }

            Book book = bookOptional.get();

            // 리뷰 페이징 조회
            Page<Review> reviewPage = reviewRepository.findByBookOrderByCreateDateDesc(book, pageable);
            PageResponseDto<ReviewResponseDto> reviewPageResponse = new PageResponseDto<>(
                    reviewPage.map((review -> convertReviewToDto(review, member)))
            );

            // ReadState 조회
            ReadState readState = null;
            if (member != null) {
                readState = getReadStateByMemberAndBook(member, book);
            }

            return BookDetailDto.builder()
                    .id(book.getId())
                    .title(book.getTitle())
                    .imageUrl(book.getImageUrl())
                    .publisher(book.getPublisher())
                    .isbn13(book.getIsbn13())
                    .totalPage(book.getTotalPage())
                    .publishedDate(book.getPublishedDate())
                    .avgRate(book.getAvgRate())
                    .categoryName(book.getCategory().getName())
                    .authors(book.getAuthors().stream()
                            .map(wrote -> wrote.getAuthor().getName())
                            .toList())
                    .readState(readState)
                    .reviews(reviewPageResponse)
                    .build();

        } catch (Exception e) {
            log.error("책 상세 조회 중 오류 발생: {}", e.getMessage());
            throw new ServiceException("500-2", "책 상세 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 책 평균 평점 업데이트
     */
    @Transactional
    public void updateBookAvgRate(Book book) {
        float avgRate = calculateAvgRateForBook(book);
        book.setAvgRate(avgRate);
        bookRepository.save(book);
        log.info("책 평균 평점 업데이트: {} -> {}", book.getTitle(), avgRate);
    }

    /**
     * Member와 Book으로 ReadState 조회
     */
    private ReadState getReadStateByMemberAndBook(Member member, Book book) {
        log.debug("ReadState 조회: memberId={}, bookId={}", member.getId(), book.getId());

        Optional<Bookmark> bookmark = bookmarkRepository.findByMemberAndBook(member, book);
        ReadState readState = bookmark.map(Bookmark::getReadState).orElse(null);

        log.debug("ReadState 결과: {}", readState);
        return readState;
    }

    /**
     * 여러 책들의 ReadState를 한번에 조회 (성능 최적화)
     */
    private Map<Integer, ReadState> getReadStatesForBooks(Member member, List<Integer> bookIds) {
        List<Bookmark> bookmarks = bookmarkRepository.findByMemberAndBookIds(member, bookIds);

        return bookmarks.stream()
                .collect(Collectors.toMap(
                        bookmark -> bookmark.getBook().getId(),
                        bookmark -> bookmark.getReadState()
                ));
    }

    /**
     * 검색어와 카테고리로 책 검색 (페이징, Member 정보 포함) - DB에서 직접 정렬 처리
     */
    public Page<BookSearchDto> searchBooksByCategory(String query, String categoryName, Pageable pageable, Member member) {
        log.info("검색어+카테고리로 유효한 책 조회: query={}, category={}, page={}, size={}, sort={}, member={}",
                query, categoryName, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(),
                member != null ? member.getId() : "null");

        try {
            // 카테고리 존재 여부 확인
            Optional<Category> categoryOptional = categoryRepository.findByName(categoryName);
            if (categoryOptional.isEmpty()) {
                log.warn("존재하지 않는 카테고리: {}", categoryName);
                return Page.empty(pageable);
            }

            // DB에서 페이징과 정렬이 적용된 조회 (검색어와 카테고리, 페이지 수가 0보다 큰 유효한 책들만)
            Page<Book> validBookPage = bookRepository.findValidBooksByQueryAndCategory(query, categoryName, pageable);

            log.info("검색 결과: {}개 책 발견 (전체 {}개)", validBookPage.getNumberOfElements(), validBookPage.getTotalElements());

            return validBookPage.map(book -> {
                BookSearchDto dto = convertToDto(book, member);
                log.debug("Book {} converted with readState: {}", book.getId(), dto.getReadState());
                return dto;
            });
        } catch (Exception e) {
            log.error("검색어+카테고리 책 조회 중 오류 발생: query={}, category={}, error={}",
                    query, categoryName, e.getMessage());
            throw new ServiceException("500-4", "검색어+카테고리 책 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * AladinBookDto를 Book 엔티티로 변환하고 저장
     */
    private Book convertAndSaveBook(AladinBookDto apiBook) {
        try {
            // 이미 존재하는 책인지 확인 (페이지 수와 관계없이)
            if (apiBook.getIsbn13() != null) {
                Optional<Book> existingBook = bookRepository.findByIsbn13(apiBook.getIsbn13());
                if (existingBook.isPresent()) {
                    log.info("이미 존재하는 ISBN: {}", apiBook.getIsbn13());
                    return existingBook.get();
                }
            }

            // Book 엔티티 생성
            Book book = new Book();
            book.setTitle(apiBook.getTitle());
            book.setImageUrl(apiBook.getImageUrl());
            book.setPublisher(apiBook.getPublisher());
            book.setIsbn13(apiBook.getIsbn13());
            book.setTotalPage(apiBook.getTotalPage());
            book.setPublishedDate(apiBook.getPublishedDate());
            book.setAvgRate(0.0f);

            // 카테고리 설정
            String categoryName = extractCategoryFromPath(apiBook.getCategoryName(), apiBook.getMallType());
            Category category = categoryRepository.findByName(categoryName)
                    .orElseGet(() -> {
                        log.info("새 카테고리 생성: {}", categoryName);
                        return categoryRepository.save(new Category(categoryName));
                    });
            book.setCategory(category);

            // 책 저장
            Book savedBook = bookRepository.save(book);
            log.info("책 저장 완료: {} (페이지: {})", savedBook.getTitle(), savedBook.getTotalPage());

            // 작가 정보 저장
            saveAuthors(apiBook.getAuthors(), savedBook);

            return savedBook;

        } catch (Exception e) {
            log.error("책 저장 중 오류: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 작가 정보 저장
     */
    private void saveAuthors(List<String> authorNames, Book book) {
        if (authorNames == null || authorNames.isEmpty()) {
            return;
        }

        for (String authorName : authorNames) {
            try {
                // 작가가 이미 존재하는지 확인
                Author author = authorRepository.findByName(authorName)
                        .orElseGet(() -> authorRepository.save(new Author(authorName)));

                // 이미 이 책과 작가의 관계가 존재하는지 확인
                boolean relationExists = wroteRepository.existsByAuthorAndBook(author, book);
                if (!relationExists) {
                    Wrote wrote = new Wrote(author, book);
                    wroteRepository.save(wrote);
                    log.info("작가-책 관계 저장: {} - {}", authorName, book.getTitle());
                }
            } catch (Exception e) {
                log.error("작가 정보 저장 중 오류: {} - {}", authorName, e.getMessage());
            }
        }
    }

    /**
     * 부족한 상세 정보 보완 및 불완전한 데이터 필터링
     */
    private List<Book> enrichMissingDetails(List<Book> books) {
        List<Book> incompleteBooks = new ArrayList<>();

        List<Book> enrichedBooks = books.stream()
                .peek(book -> {
                    if (needsDetailEnrichment(book)) {
                        log.info("상세 정보 보완 시도: {} (ISBN: {})", book.getTitle(), book.getIsbn13());
                        enrichBookWithDetailInfo(book);
                    }
                })
                .filter(book -> {
                    // 상세 정보 보완 후에도 페이지 수가 0인 책은 제외
                    if (book.getTotalPage() == 0) {
                        log.warn("페이지 수가 0인 책을 목록에서 제외: {} (ISBN: {})",
                                book.getTitle(), book.getIsbn13());
                        incompleteBooks.add(book);
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // 불완전한 책들의 정보를 로그로 남김 (나중에 배치 처리 가능)
        if (!incompleteBooks.isEmpty()) {
            log.info("불완전한 책 {}권이 발견되어 검색 결과에서 제외됨", incompleteBooks.size());
            for (Book book : incompleteBooks) {
                log.warn("정리 대상 책 ID: {} - {} (페이지: {})",
                        book.getId(), book.getTitle(), book.getTotalPage());
            }
        }

        return enrichedBooks;
    }

    /**
     * 상세 정보 보완이 필요한지 판단
     */
    private boolean needsDetailEnrichment(Book book) {
        return book.getIsbn13() != null &&
                (book.getTotalPage() == 0 || book.getAuthors().isEmpty());
    }

    /**
     * 개별 ISBN 조회로 상세 정보 보완
     */
    private void enrichBookWithDetailInfo(Book book) {
        AladinBookDto detailBook = aladinApiClient.getBookDetails(book.getIsbn13());
        if (detailBook == null) {
            log.warn("상세 정보 조회 실패: {} (ISBN: {})", book.getTitle(), book.getIsbn13());
            return;
        }

        boolean updated = false;

        // 페이지 수 보완
        if (book.getTotalPage() == 0 && detailBook.getTotalPage() > 0) {
            book.setTotalPage(detailBook.getTotalPage());
            updated = true;
            log.info("페이지 수 보완: {} -> {}", book.getTitle(), detailBook.getTotalPage());
        }

        // 저자 정보 보완
        if (book.getAuthors().isEmpty() && detailBook.getAuthors() != null && !detailBook.getAuthors().isEmpty()) {
            saveAuthors(detailBook.getAuthors(), book);
            updated = true;
            log.info("저자 정보 보완: {} -> {}", book.getTitle(), detailBook.getAuthors());
        }

        if (updated) {
            bookRepository.save(book);
            log.info("상세 정보 보완 완료: {} (페이지: {}, 저자: {})",
                    book.getTitle(), book.getTotalPage(), book.getAuthors().size());
        }
    }

    /**
     * 카테고리 경로에서 2번째 깊이 추출
     */
    private String extractCategoryFromPath(String categoryName, String mallType) {
        if (categoryName != null && !categoryName.isEmpty()) {
            log.debug("원본 카테고리 경로: {}", categoryName);

            String[] categoryParts = categoryName.split(">");

            if (categoryParts.length > 1) {
                String secondLevelCategory = categoryParts[1].trim();
                log.debug("추출된 카테고리: {}", secondLevelCategory);
                return secondLevelCategory;
            }

            if (categoryParts.length > 0) {
                String firstLevelCategory = categoryParts[0].trim();
                log.debug("첫 번째 레벨 카테고리 사용: {}", firstLevelCategory);
                return firstLevelCategory;
            }
        }

        // 기본 카테고리
        String fallbackCategory = getFallbackCategory(mallType);
        log.debug("기본 카테고리 사용: {}", fallbackCategory);
        return fallbackCategory;
    }

    /**
     * mallType 기반 기본 카테고리 (eBook 제거)
     */
    private String getFallbackCategory(String mallType) {
        if (mallType == null) {
            return "기타";
        }

        switch (mallType) {
            case "BOOK":
                return "국내도서";
            case "FOREIGN":
                return "외국도서";
            default:
                return "기타";
        }
    }

    /**
     * 카테고리별 책 조회 (페이징, Member 정보 포함) - DB에서 직접 정렬 처리
     */
    public Page<BookSearchDto> getBooksByCategory(String categoryName, Pageable pageable, Member member) {
        log.info("카테고리별 유효한 책 조회: category={}, page={}, size={}, sort={}, member={}",
                categoryName, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(),
                member != null ? member.getId() : "null");

        try {
            // 카테고리 존재 여부 확인
            Optional<Category> categoryOptional = categoryRepository.findByName(categoryName);
            if (categoryOptional.isEmpty()) {
                log.warn("존재하지 않는 카테고리: {}", categoryName);
                return Page.empty(pageable);
            }

            // DB에서 페이징과 정렬이 적용된 조회 (페이지 수가 0보다 큰 유효한 책들만)
            Page<Book> validBookPage = bookRepository.findValidBooksByCategory(categoryName, pageable);

            return validBookPage.map(book -> {
                BookSearchDto dto = convertToDto(book, member);
                log.debug("Book {} converted with readState: {}", book.getId(), dto.getReadState());
                return dto;
            });
        } catch (Exception e) {
            log.error("카테고리별 책 조회 중 오류 발생: {}", e.getMessage());
            throw new ServiceException("500-3", "카테고리별 책 조회 중 오류가 발생했습니다.");
        }
    }


    /**
     * 책의 평균 평점 계산
     */
    private float calculateAvgRateForBook(Book book) {
        List<Review> reviews = book.getReviews();
        if (reviews == null || reviews.isEmpty()) {
            return 0.0f;
        }

        double average = reviews.stream()
                .mapToInt(Review::getRate)
                .average()
                .orElse(0.0);

        return (float) average;
    }

    /**
     * Book 엔티티 리스트를 DTO로 변환 (Member 정보 포함)
     */
    private List<BookSearchDto> convertToDto(List<Book> books, Member member) {
        if (member == null) {
            return books.stream()
                    .map(book -> convertToDto(book, (ReadState) null))
                    .toList();
        }

        // 성능 최적화: 한번에 모든 책의 ReadState 조회
        List<Integer> bookIds = books.stream()
                .map(Book::getId)
                .collect(Collectors.toList());

        Map<Integer, ReadState> readStatesMap = getReadStatesForBooks(member, bookIds);

        return books.stream()
                .map(book -> {
                    ReadState readState = readStatesMap.get(book.getId());
                    return convertToDto(book, readState);
                })
                .toList();
    }

    /**
     * 단일 Book 엔티티를 DTO로 변환 (Member 정보 포함)
     */
    private BookSearchDto convertToDto(Book book, Member member) {
        ReadState readState = null;
        if (member != null) {
            readState = getReadStateByMemberAndBook(member, book);
        }
        return convertToDto(book, readState);
    }

    /**
     * 단일 Book 엔티티를 DTO로 변환 (ReadState 직접 전달)
     */
    private BookSearchDto convertToDto(Book book, ReadState readState) {
        return BookSearchDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .imageUrl(book.getImageUrl())
                .publisher(book.getPublisher())
                .isbn13(book.getIsbn13())
                .totalPage(book.getTotalPage())
                .publishedDate(book.getPublishedDate())
                .avgRate(book.getAvgRate())
                .categoryName(book.getCategory().getName())
                .authors(book.getAuthors().stream()
                        .map(wrote -> wrote.getAuthor().getName())
                        .toList())
                .readState(readState)
                .build();
    }

    /**
     * Review 엔티티를 DTO로 변환
     */
    private ReviewResponseDto convertReviewToDto(Review review, Member member) {
        return ReviewResponseDto.builder()
                .id(review.getId())
                .content(review.getContent())
                .rate(review.getRate())
                .memberName(review.getMember().getName())
                .memberId(review.getMember().getId())
                .likeCount(review.getLikeCount())
                .dislikeCount(review.getDislikeCount())
                .isRecommended(reviewRecommendService.isRecommended(review, member))
                .createdDate(review.getCreateDate())
                .modifiedDate(review.getModifyDate())
                .build();
    }
}