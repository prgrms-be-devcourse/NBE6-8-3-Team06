package com.back.domain.book.book.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

/**
 * 알라딘 API 수동 테스트 클래스
 * 실제 API 호출을 통해 응답을 확인할 수 있습니다.
 */
class AladinApiManualTest {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String API_KEY = "ttbsnake10101245001"; // 실제 키로 변경 필요
    private final String BASE_URL = "http://www.aladin.co.kr/ttb/api";

    @Test
    @DisplayName("알라딘 API 책 검색 테스트 - 작가 정보 포함")
    void testAladinBookSearchWithAuthors() {

        try {
            // 검색 URL 구성 (authors 정보 요청)
            String url = String.format(
                    "%s/ItemSearch.aspx?ttbkey=%s&Query=%s&QueryType=Title&MaxResults=%d&start=%d&SearchTarget=Book&output=js&Version=20131101&OptResult=authors",
                    BASE_URL, API_KEY, "해리", 5, 1
            );

            System.out.println("요청 URL: " + url);

            // API 호출
            String response = restTemplate.getForObject(url, String.class);

            System.out.println("API 응답:");
            System.out.println(response);

            // JSON 파싱
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode itemsNode = rootNode.get("item");

            if (itemsNode != null && itemsNode.isArray()) {
                System.out.println("\n검색된 책 목록:");
                for (int i = 0; i < itemsNode.size(); i++) {
                    JsonNode item = itemsNode.get(i);
                    String title = getJsonValue(item, "title");
                    String author = getJsonValue(item, "author");
                    String publisher = getJsonValue(item, "publisher");
                    String isbn13 = getJsonValue(item, "isbn13");
                    String cover = getJsonValue(item, "cover");
                    String categoryName = getJsonValue(item, "categoryName");
                    String mallType = getJsonValue(item, "mallType");

                    System.out.printf("%d. 제목: %s%n", i + 1, title);
                    System.out.printf("   작가: %s%n", author);
                    System.out.printf("   출판사: %s%n", publisher);
                    System.out.printf("   ISBN13: %s%n", isbn13);
                    System.out.printf("   카테고리: %s%n", categoryName);
                    System.out.printf("   몰타입: %s%n", mallType);
                    System.out.printf("   표지: %s%n", cover);

                    // 작가 상세 정보 확인
                    JsonNode subInfoNode = item.get("subInfo");
                    if (subInfoNode != null) {
                        JsonNode authorsNode = subInfoNode.get("authors");
                        if (authorsNode != null && authorsNode.isArray()) {
                            System.out.println("   상세 작가 정보:");
                            for (JsonNode authorNode : authorsNode) {
                                String authorName = getJsonValue(authorNode, "authorName");
                                String authorType = getJsonValue(authorNode, "authorType");
                                System.out.printf("     - %s (%s)%n", authorName, authorType);
                            }
                        }
                    }
                    System.out.println();
                }
            } else {
                System.out.println("검색 결과가 없습니다.");
            }

        } catch (Exception e) {
            System.err.println("API 호출 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("알라딘 API ISBN으로 책 조회 테스트 - 작가 및 카테고리 정보 포함")
    void testAladinBookLookupWithDetails() {

        try {
            String isbn = "9788966261024"; // 테스트용 ISBN
            String url = String.format(
                    "%s/ItemLookUp.aspx?ttbkey=%s&itemIdType=ISBN13&ItemId=%s&output=js&Version=20131101&OptResult=authors,packing,categoryIdList",
                    BASE_URL, API_KEY, isbn
            );

            System.out.println("요청 URL: " + url);

            // API 호출
            String response = restTemplate.getForObject(url, String.class);

            System.out.println("API 응답:");
            System.out.println(response);

            // JSON 파싱
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode itemsNode = rootNode.get("item");

            if (itemsNode != null && itemsNode.isArray() && itemsNode.size() > 0) {
                JsonNode item = itemsNode.get(0);

                System.out.println("\n조회된 책 정보:");
                System.out.println("제목: " + getJsonValue(item, "title"));
                System.out.println("작가: " + getJsonValue(item, "author"));
                System.out.println("출판사: " + getJsonValue(item, "publisher"));
                System.out.println("ISBN13: " + getJsonValue(item, "isbn13"));
                System.out.println("총 페이지: " + getJsonValue(item, "itemPage"));
                System.out.println("출간일: " + getJsonValue(item, "pubDate"));
                System.out.println("평점: " + getJsonValue(item, "customerReviewRank"));
                System.out.println("몰타입: " + getJsonValue(item, "mallType"));
                System.out.println("표지: " + getJsonValue(item, "cover"));

                // 부가 정보 확인
                JsonNode subInfoNode = item.get("subInfo");
                if (subInfoNode != null) {
                    System.out.println("\n부가 정보:");
                    System.out.println("부제: " + getJsonValue(subInfoNode, "subTitle"));
                    System.out.println("쪽수: " + getJsonValue(subInfoNode, "itemPage"));

                    // 작가 상세 정보
                    JsonNode authorsNode = subInfoNode.get("authors");
                    if (authorsNode != null && authorsNode.isArray()) {
                        System.out.println("\n작가 상세 정보:");
                        for (JsonNode authorNode : authorsNode) {
                            String authorName = getJsonValue(authorNode, "authorName");
                            String authorType = getJsonValue(authorNode, "authorType");
                            String authorTypeDesc = getJsonValue(authorNode, "authorTypeDesc");
                            System.out.printf("- %s (%s: %s)%n", authorName, authorType, authorTypeDesc);
                        }
                    }
                }

                // 카테고리 정보 확인
                JsonNode categoryIdListNode = subInfoNode != null ? subInfoNode.get("categoryIdList") : null;
                if (categoryIdListNode != null && categoryIdListNode.isArray()) {
                    System.out.println("\n카테고리 정보:");
                    for (JsonNode categoryNode : categoryIdListNode) {
                        JsonNode categoryInfo = categoryNode.get("categoryInfo");
                        if (categoryInfo != null) {
                            String categoryId = getJsonValue(categoryInfo, "categoryId");
                            String categoryName = getJsonValue(categoryInfo, "categoryName");
                            System.out.printf("- %s (ID: %s)%n", categoryName, categoryId);
                        }
                    }
                }

            } else {
                System.out.println("해당 ISBN의 책을 찾을 수 없습니다.");
            }

        } catch (Exception e) {
            System.err.println("API 호출 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("도서 관련 타입만 검색 테스트")
    void testBookTypesOnlySearch() {
        try {
            String[] bookSearchTargets = {"Book", "Foreign", "eBook"};
            String[] expectedCategories = {"국내도서", "외국도서", "전자책"};

            for (int i = 0; i < bookSearchTargets.length; i++) {
                String searchTarget = bookSearchTargets[i];
                String expectedCategory = expectedCategories[i];

                System.out.printf("\n=== %s (%s) 검색 테스트 ===\n", searchTarget, expectedCategory);

                String url = String.format(
                        "%s/ItemSearch.aspx?ttbkey=%s&Query=test&QueryType=Title&MaxResults=3&SearchTarget=%s&output=js&Version=20131101",
                        BASE_URL, API_KEY, searchTarget
                );

                String response = restTemplate.getForObject(url, String.class);
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode itemsNode = rootNode.get("item");

                if (itemsNode != null && itemsNode.isArray() && itemsNode.size() > 0) {
                    for (JsonNode item : itemsNode) {
                        String title = getJsonValue(item, "title");
                        String author = getJsonValue(item, "author");
                        String mallType = getJsonValue(item, "mallType");
                        String categoryName = getJsonValue(item, "categoryName");

                        System.out.printf("제목: %s%n", title);
                        System.out.printf("작가: %s%n", author);
                        System.out.printf("mallType: %s%n", mallType);
                        System.out.printf("카테고리: %s%n", categoryName != null ? categoryName : expectedCategory);
                        System.out.println();
                    }
                } else {
                    System.out.printf("%s 타입의 검색 결과가 없습니다.%n", searchTarget);
                }

                // API 호출 제한을 고려한 딜레이
                Thread.sleep(100);
            }

        } catch (Exception e) {
            System.err.println("도서 타입 검색 테스트 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("알라딘 API 연결 테스트")
    void testAladinConnection() {
        try {
            // 가장 기본적인 검색 테스트
            String url = String.format(
                    "%s/ItemSearch.aspx?ttbkey=%s&Query=test&QueryType=Title&MaxResults=1&SearchTarget=Book&output=js&Version=20131101",
                    BASE_URL, API_KEY
            );

            String response = restTemplate.getForObject(url, String.class);

            if (response != null && response.contains("item")) {
                System.out.println("✅ 알라딘 API 연결 성공!");

                // 응답 구조 확인
                JsonNode rootNode = objectMapper.readTree(response);
                System.out.println("API 버전: " + rootNode.get("version"));
                System.out.println("총 결과 수: " + rootNode.get("totalResults"));

            } else {
                System.out.println("❌ 알라딘 API 응답이 예상과 다릅니다.");
                System.out.println("응답: " + response);
            }

        } catch (Exception e) {
            System.err.println("❌ 알라딘 API 연결 실패: " + e.getMessage());

            // 일반적인 오류 원인 안내
            System.err.println("\n가능한 원인:");
            System.err.println("1. API 키가 올바르지 않음");
            System.err.println("2. 네트워크 연결 문제");
            System.err.println("3. 알라딘 API 서버 문제");
            System.err.println("4. 요청 형식 오류");
        }
    }

    private String getJsonValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return (fieldNode != null && !fieldNode.isNull()) ? fieldNode.asText() : null;
    }
}