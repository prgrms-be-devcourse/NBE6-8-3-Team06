package com.back.domain.book.client.aladin;

import com.back.domain.book.client.aladin.dto.AladinBookDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AladinApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${aladin.api.key}")
    private String aladinApiKey;

    @Value("${aladin.api.base-url}")
    private String aladinBaseUrl;

    // API 엔드포인트 enum
    public enum ApiEndpoint {
        ITEM_SEARCH("ItemSearch.aspx"),
        ITEM_LOOKUP("ItemLookUp.aspx");

        private final String endpoint;

        ApiEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getEndpoint() {
            return endpoint;
        }
    }

    // 검색 대상 enum (eBook 제거)
    public enum SearchTarget {
        BOOK("Book", "국내도서"),
        FOREIGN("Foreign", "외국도서");

        private final String target;
        private final String displayName;

        SearchTarget(String target, String displayName) {
            this.target = target;
            this.displayName = displayName;
        }

        public String getTarget() {
            return target;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 검색어로 책 검색
     */
    public List<AladinBookDto> searchBooks(String query, int limit) {
        List<AladinBookDto> allBooks = new ArrayList<>();
        int limitPerCategory = Math.max(1, limit / 2); // 2개 카테고리로 변경

        try {
            for (SearchTarget searchTarget : SearchTarget.values()) {
                String url = buildSearchUrl(query, limitPerCategory, searchTarget);
                List<AladinBookDto> books = callApiAndParseBooks(url, searchTarget.getDisplayName());
                allBooks.addAll(books);
            }
        } catch (Exception e) {
            log.error("알라딘 API 검색 중 오류: {}", e.getMessage());
        }

        return allBooks.stream().limit(limit).toList();
    }

    /**
     * ISBN으로 책 조회
     */
    public AladinBookDto getBookByIsbn(String isbn) {
        String url = buildIsbnLookupUrl(isbn);
        List<AladinBookDto> books = callApiAndParseBooks(url, "ISBN조회");
        return books.isEmpty() ? null : books.get(0);
    }

    /**
     * 책 상세 정보 조회 (페이지 수, 저자 정보 등)
     */
    public AladinBookDto getBookDetails(String isbn) {
        try {
            String url = buildIsbnLookupUrl(isbn);
            String response = restTemplate.getForObject(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode itemsNode = rootNode.get("item");

            if (itemsNode != null && itemsNode.isArray() && itemsNode.size() > 0) {
                JsonNode itemNode = itemsNode.get(0);
                return parseBookFromJson(itemNode);
            }
        } catch (Exception e) {
            log.warn("상세 정보 조회 실패: ISBN {}, Error: {}", isbn, e.getMessage());
        }

        return null;
    }

    /**
     * 검색 API URL 생성
     */
    private String buildSearchUrl(String query, int maxResults, SearchTarget searchTarget) {
        return String.format(
                "%s/%s?ttbkey=%s&Query=%s&QueryType=Title&MaxResults=%d&start=1&SearchTarget=%s&output=js&Version=20131101&OptResult=authors",
                aladinBaseUrl,
                ApiEndpoint.ITEM_SEARCH.getEndpoint(),
                aladinApiKey,
                query,
                maxResults,
                searchTarget.getTarget()
        );
    }

    /**
     * ISBN 조회 API URL 생성
     */
    private String buildIsbnLookupUrl(String isbn) {
        return String.format(
                "%s/%s?ttbkey=%s&itemIdType=ISBN13&ItemId=%s&output=js&Version=20131101&OptResult=authors",
                aladinBaseUrl,
                ApiEndpoint.ITEM_LOOKUP.getEndpoint(),
                aladinApiKey,
                isbn
        );
    }

    /**
     * API 호출 및 파싱 공통 메서드
     */
    private List<AladinBookDto> callApiAndParseBooks(String url, String searchType) {
        try {
            log.debug("{} 검색 API 호출: {}", searchType, url);
            String response = restTemplate.getForObject(url, String.class);
            return parseApiResponse(response);
        } catch (Exception e) {
            log.error("{} API 호출 중 오류: {}", searchType, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * API 응답 파싱
     */
    private List<AladinBookDto> parseApiResponse(String response) {
        List<AladinBookDto> books = new ArrayList<>();

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode itemsNode = rootNode.get("item");

            if (itemsNode != null && itemsNode.isArray()) {
                for (JsonNode itemNode : itemsNode) {
                    AladinBookDto book = parseBookFromJson(itemNode);
                    if (book != null) {
                        books.add(book);
                    }
                }
            }
        } catch (Exception e) {
            log.error("API 응답 파싱 중 오류: {}", e.getMessage());
        }

        return books;
    }

    /**
     * JSON에서 AladinBookDto 생성
     */
    private AladinBookDto parseBookFromJson(JsonNode itemNode) {
        try {
            // mallType 체크 - 도서 관련 타입이 아니면 null 반환
            String mallType = getJsonValue(itemNode, "mallType");
            if (mallType != null && !isBookRelatedType(mallType)) {
                log.debug("도서가 아닌 타입이므로 건너뜀: {}", mallType);
                return null;
            }

            AladinBookDto.AladinBookDtoBuilder builder = AladinBookDto.builder()
                    .title(getJsonValue(itemNode, "title"))
                    .imageUrl(getJsonValue(itemNode, "cover"))
                    .publisher(getJsonValue(itemNode, "publisher"))
                    .categoryName(getJsonValue(itemNode, "categoryName"))
                    .mallType(mallType);

            // ISBN 설정
            String isbn13 = getJsonValue(itemNode, "isbn13");
            if (isbn13 != null && !isbn13.isEmpty()) {
                builder.isbn13(isbn13);
            } else {
                String isbn = getJsonValue(itemNode, "isbn");
                if (isbn != null && isbn.length() == 13) {
                    builder.isbn13(isbn);
                }
            }

            // 페이지 수 설정
            builder.totalPage(extractPageInfo(itemNode));

            // 출간일 설정
            String pubDateStr = getJsonValue(itemNode, "pubDate");
            if (pubDateStr != null && !pubDateStr.isEmpty()) {
                try {
                    LocalDateTime pubDate = parsePubDate(pubDateStr);
                    builder.publishedDate(pubDate);
                } catch (Exception e) {
                    log.warn("출간일 파싱 실패: {}", pubDateStr);
                }
            }

            // 저자 정보 설정
            builder.authors(extractAuthors(itemNode));

            return builder.build();

        } catch (Exception e) {
            log.error("AladinBookDto 생성 중 오류: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 페이지 정보 추출
     */
    private int extractPageInfo(JsonNode itemNode) {
        // 기본 itemPage 먼저 확인
        JsonNode totalPageNode = itemNode.get("itemPage");
        if (totalPageNode != null && !totalPageNode.isNull()) {
            return totalPageNode.asInt();
        }

        // subInfo의 itemPage 확인
        JsonNode subInfoNode = itemNode.get("subInfo");
        if (subInfoNode != null) {
            JsonNode subPageNode = subInfoNode.get("itemPage");
            if (subPageNode != null && !subPageNode.isNull()) {
                return subPageNode.asInt();
            }
        }

        return 0;
    }

    /**
     * 저자 정보 추출
     */
    private List<String> extractAuthors(JsonNode itemNode) {
        List<String> authorNames = new ArrayList<>();

        // 기본 author 필드에서 작가 정보 추출
        String authorString = getJsonValue(itemNode, "author");
        if (authorString != null && !authorString.isEmpty()) {
            String[] authors = authorString.split("[,;]");
            for (String authorName : authors) {
                String trimmedName = authorName.trim();
                if (!trimmedName.isEmpty()) {
                    authorNames.add(trimmedName);
                }
            }
        }

        // subInfo의 authors 배열에서 상세 작가 정보 추출
        JsonNode subInfoNode = itemNode.get("subInfo");
        if (subInfoNode != null) {
            JsonNode authorsNode = subInfoNode.get("authors");
            if (authorsNode != null && authorsNode.isArray()) {
                for (JsonNode authorNode : authorsNode) {
                    String authorName = getJsonValue(authorNode, "authorName");
                    if (authorName != null && !authorName.isEmpty()) {
                        authorNames.add(authorName.trim());
                    }
                }
            }
        }

        // 중복 제거
        return authorNames.stream().distinct().toList();
    }

    /**
     * 도서 관련 타입인지 확인 (eBook 제거)
     */
    private boolean isBookRelatedType(String mallType) {
        return "BOOK".equals(mallType) ||
                "FOREIGN".equals(mallType);
    }

    /**
     * JSON에서 문자열 값 추출
     */
    private String getJsonValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return (fieldNode != null && !fieldNode.isNull()) ? fieldNode.asText() : null;
    }

    /**
     * 출간일 파싱 (다양한 형식 지원)
     */
    private LocalDateTime parsePubDate(String pubDateStr) {
        try {
            if (pubDateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDateTime.parse(pubDateStr + "T00:00:00");
            }

            if (pubDateStr.matches("\\d{4}-\\d{2}")) {
                return LocalDateTime.parse(pubDateStr + "-01T00:00:00");
            }

            if (pubDateStr.matches("\\d{4}")) {
                return LocalDateTime.parse(pubDateStr + "-01-01T00:00:00");
            }

            return LocalDateTime.now();

        } catch (Exception e) {
            log.warn("날짜 파싱 실패, 현재 시간으로 설정: {}", pubDateStr);
            return LocalDateTime.now();
        }
    }
}