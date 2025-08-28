package com.back.domain.book.client.aladin

import com.back.domain.book.client.aladin.dto.AladinBookDto
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import kotlin.math.max

@Component
class AladinApiClient(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper
) {

    @Value("\${aladin.api.key}")
    private lateinit var aladinApiKey: String

    @Value("\${aladin.api.base-url}")
    private lateinit var aladinBaseUrl: String

    companion object {
        private val log = LoggerFactory.getLogger(AladinApiClient::class.java)
    }

    // API 엔드포인트 enum
    enum class ApiEndpoint(val endpoint: String) {
        ITEM_SEARCH("ItemSearch.aspx"),
        ITEM_LOOKUP("ItemLookUp.aspx")
    }

    // 검색 대상 enum (eBook 제거)
    enum class SearchTarget(val target: String, val displayName: String) {
        BOOK("Book", "국내도서"),
        FOREIGN("Foreign", "외국도서")
    }

    /**
     * 검색어로 책 검색
     */
    fun searchBooks(query: String?, limit: Int): List<AladinBookDto> {
        val allBooks = mutableListOf<AladinBookDto>()
        val limitPerCategory = max(1, limit / 2) // 2개 카테고리로 변경

        try {
            SearchTarget.entries.forEach { searchTarget ->
                val url = buildSearchUrl(query, limitPerCategory, searchTarget)
                val books = callApiAndParseBooks(url, searchTarget.displayName)
                allBooks.addAll(books)
            }
        } catch (e: Exception) {
            log.error("알라딘 API 검색 중 오류: {}", e.message)
        }

        return allBooks.take(limit)
    }

    /**
     * ISBN으로 책 조회
     */
    fun getBookByIsbn(isbn: String?): AladinBookDto? {
        val url = buildIsbnLookupUrl(isbn)
        val books = callApiAndParseBooks(url, "ISBN조회")
        return books.firstOrNull()
    }

    /**
     * 책 상세 정보 조회 (페이지 수, 저자 정보 등)
     */
    fun getBookDetails(isbn: String?): AladinBookDto? {
        return try {
            val url = buildIsbnLookupUrl(isbn)
            val response = restTemplate.getForObject(url, String::class.java)
            val rootNode = objectMapper.readTree(response)
            val itemsNode = rootNode.get("item")

            if (itemsNode?.isArray == true && itemsNode.size() > 0) {
                parseBookFromJson(itemsNode[0])
            } else null
        } catch (e: Exception) {
            log.warn("상세 정보 조회 실패: ISBN {}, Error: {}", isbn, e.message)
            null
        }
    }

    /**
     * 검색 API URL 생성
     */
    private fun buildSearchUrl(query: String?, maxResults: Int, searchTarget: SearchTarget): String {
        return "$aladinBaseUrl/${ApiEndpoint.ITEM_SEARCH.endpoint}" +
                "?ttbkey=$aladinApiKey" +
                "&Query=$query" +
                "&QueryType=Title" +
                "&MaxResults=$maxResults" +
                "&start=1" +
                "&SearchTarget=${searchTarget.target}" +
                "&output=js" +
                "&Version=20131101" +
                "&OptResult=authors"
    }

    /**
     * ISBN 조회 API URL 생성
     */
    private fun buildIsbnLookupUrl(isbn: String?): String {
        return "$aladinBaseUrl/${ApiEndpoint.ITEM_LOOKUP.endpoint}" +
                "?ttbkey=$aladinApiKey" +
                "&itemIdType=ISBN13" +
                "&ItemId=$isbn" +
                "&output=js" +
                "&Version=20131101" +
                "&OptResult=authors"
    }

    /**
     * API 호출 및 파싱 공통 메서드
     */
    private fun callApiAndParseBooks(url: String, searchType: String): List<AladinBookDto> {
        return try {
            log.debug("{} 검색 API 호출: {}", searchType, url)
            val response = restTemplate.getForObject(url, String::class.java)
            parseApiResponse(response)
        } catch (e: Exception) {
            log.error("{} API 호출 중 오류: {}", searchType, e.message)
            emptyList()
        }
    }

    /**
     * API 응답 파싱
     */
    private fun parseApiResponse(response: String?): List<AladinBookDto> {
        if (response == null) return emptyList()

        return try {
            val rootNode = objectMapper.readTree(response)
            val itemsNode = rootNode.get("item")

            if (itemsNode?.isArray == true) {
                itemsNode.mapNotNull { parseBookFromJson(it) }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            log.error("API 응답 파싱 중 오류: {}", e.message)
            emptyList()
        }
    }

    /**
     * JSON에서 AladinBookDto 생성
     */
    private fun parseBookFromJson(itemNode: JsonNode): AladinBookDto? {
        return try {
            // mallType 체크 - 도서 관련 타입이 아니면 null 반환
            val mallType = getJsonValue(itemNode, "mallType")
            if (mallType != null && !isBookRelatedType(mallType)) {
                log.debug("도서가 아닌 타입이므로 건너뜀: {}", mallType)
                return null
            }

            // ISBN 설정
            val isbn13 = getJsonValue(itemNode, "isbn13")?.takeIf { it.isNotEmpty() }
                ?: getJsonValue(itemNode, "isbn")?.takeIf { it.length == 13 }

            // 출간일 설정
            val publishedDate = getJsonValue(itemNode, "pubDate")
                ?.takeIf { it.isNotEmpty() }
                ?.let { pubDateStr ->
                    try {
                        parsePubDate(pubDateStr)
                    } catch (e: Exception) {
                        log.warn("출간일 파싱 실패: {}", pubDateStr)
                        null
                    }
                }

            AladinBookDto(
                title = getJsonValue(itemNode, "title"),
                imageUrl = getJsonValue(itemNode, "cover"),
                publisher = getJsonValue(itemNode, "publisher"),
                isbn13 = isbn13,
                totalPage = extractPageInfo(itemNode),
                publishedDate = publishedDate,
                categoryName = getJsonValue(itemNode, "categoryName"),
                mallType = mallType,
                authors = extractAuthors(itemNode)
            )
        } catch (e: Exception) {
            log.error("AladinBookDto 생성 중 오류: {}", e.message)
            null
        }
    }

    /**
     * 페이지 정보 추출
     */
    private fun extractPageInfo(itemNode: JsonNode): Int {
        // 기본 itemPage 먼저 확인
        itemNode.get("itemPage")?.takeIf { !it.isNull }?.let {
            return it.asInt()
        }

        // subInfo의 itemPage 확인
        itemNode.get("subInfo")?.get("itemPage")?.takeIf { !it.isNull }?.let {
            return it.asInt()
        }

        return 0
    }

    /**
     * 저자 정보 추출
     */
    private fun extractAuthors(itemNode: JsonNode): List<String> {
        val authorNames = mutableSetOf<String>()

        // 기본 author 필드에서 작가 정보 추출
        getJsonValue(itemNode, "author")
            ?.takeIf { it.isNotEmpty() }
            ?.split(",", ";")
            ?.mapNotNull { it.trim().takeIf { name -> name.isNotEmpty() } }
            ?.let { authorNames.addAll(it) }

        // subInfo의 authors 배열에서 상세 작가 정보 추출
        itemNode.get("subInfo")?.get("authors")?.takeIf { it.isArray }?.let { authorsNode ->
            authorsNode.mapNotNull { authorNode ->
                getJsonValue(authorNode, "authorName")
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
            }.let { authorNames.addAll(it) }
        }

        return authorNames.toList()
    }

    /**
     * 도서 관련 타입인지 확인 (eBook 제거)
     */
    private fun isBookRelatedType(mallType: String): Boolean {
        return mallType in setOf("BOOK", "FOREIGN")
    }

    /**
     * JSON에서 문자열 값 추출
     */
    private fun getJsonValue(node: JsonNode, fieldName: String): String? {
        return node.get(fieldName)?.takeIf { !it.isNull }?.asText()
    }

    /**
     * 출간일 파싱 (다양한 형식 지원)
     */
    private fun parsePubDate(pubDateStr: String): LocalDateTime {
        return try {
            when {
                pubDateStr.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) ->
                    LocalDateTime.parse("${pubDateStr}T00:00:00")

                pubDateStr.matches(Regex("\\d{4}-\\d{2}")) ->
                    LocalDateTime.parse("${pubDateStr}-01T00:00:00")

                pubDateStr.matches(Regex("\\d{4}")) ->
                    LocalDateTime.parse("${pubDateStr}-01-01T00:00:00")

                else -> LocalDateTime.now()
            }
        } catch (e: Exception) {
            log.warn("날짜 파싱 실패, 현재 시간으로 설정: {}", pubDateStr)
            LocalDateTime.now()
        }
    }
}