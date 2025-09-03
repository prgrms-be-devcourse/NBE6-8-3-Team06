package com.back.domain.book.book.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestTemplate

/**
 * 알라딘 API 수동 테스트 클래스
 * 실제 API 호출을 통해 응답을 확인할 수 있습니다.
 */
internal class AladinApiManualTest {

    private val restTemplate = RestTemplate()
    private val objectMapper = ObjectMapper()

    companion object {
        private const val API_KEY = "ttbsnake10101245001" // 실제 키로 변경 필요
        private const val BASE_URL = "http://www.aladin.co.kr/ttb/api"
        private const val API_DELAY = 100L
    }

    @Test
    @DisplayName("알라딘 API 책 검색 테스트 - 작가 정보 포함")
    fun testAladinBookSearchWithAuthors() {
        try {
            val url = buildSearchUrl(
                query = "해리",
                maxResults = 5,
                optResult = "authors"
            )

            println("요청 URL: $url")

            val response = restTemplate.getForObject(url, String::class.java)
                ?: return println("응답이 null입니다.")

            println("API 응답:\n$response")

            val items = parseItemsFromResponse(response)
            if (items.isEmpty()) {
                println("검색 결과가 없습니다.")
                return
            }

            println("\n검색된 책 목록:")
            items.forEachIndexed { index, item ->
                printBookInfo(index + 1, item)
                printAuthorDetails(item)
                println()
            }

        } catch (e: Exception) {
            handleApiError("책 검색 테스트", e)
        }
    }

    @Test
    @DisplayName("알라딘 API ISBN으로 책 조회 테스트 - 작가 및 카테고리 정보 포함")
    fun testAladinBookLookupWithDetails() {
        try {
            val isbn = "9788966261024"
            val url = buildLookupUrl(isbn, "authors,packing,categoryIdList")

            println("요청 URL: $url")

            val response = restTemplate.getForObject(url, String::class.java)
                ?: return println("응답이 null입니다.")

            println("API 응답:\n$response")

            val items = parseItemsFromResponse(response)
            val item = items.firstOrNull()
                ?: return println("해당 ISBN의 책을 찾을 수 없습니다.")

            printDetailedBookInfo(item)

        } catch (e: Exception) {
            handleApiError("ISBN 조회 테스트", e)
        }
    }

    @Test
    @DisplayName("도서 관련 타입만 검색 테스트")
    fun testBookTypesOnlySearch() {
        try {
            val searchConfigs = listOf(
                "Book" to "국내도서",
                "Foreign" to "외국도서",
                "eBook" to "전자책"
            )

            searchConfigs.forEach { (searchTarget, expectedCategory) ->
                println("\n=== $searchTarget ($expectedCategory) 검색 테스트 ===")

                val url = buildSearchUrl(
                    query = "test",
                    maxResults = 3,
                    searchTarget = searchTarget
                )

                val response = restTemplate.getForObject(url, String::class.java)
                    ?: return@forEach println("응답이 null입니다.")

                val items = parseItemsFromResponse(response)

                if (items.isNotEmpty()) {
                    items.forEach { item ->
                        printSimpleBookInfo(item, expectedCategory)
                        println()
                    }
                } else {
                    println("$searchTarget 타입의 검색 결과가 없습니다.")
                }

                Thread.sleep(API_DELAY)
            }

        } catch (e: Exception) {
            handleApiError("도서 타입 검색 테스트", e)
        }
    }

    @Test
    @DisplayName("알라딘 API 카테고리별 검색 테스트")
    fun testAladinCategorySearch() {
        try {
            val categoryConfigs = listOf(
                170 to "문학",
                798 to "어린이",
                987 to "경제경영",
                656 to "종교/역학"
            )

            categoryConfigs.forEach { (categoryId, categoryName) ->
                println("\n=== 카테고리 $categoryName (ID: $categoryId) 검색 테스트 ===")

                val url = buildCategoryListUrl(categoryId = categoryId, maxResults = 3)
                println("요청 URL: $url")

                val response = restTemplate.getForObject(url, String::class.java)
                    ?: return@forEach println("응답이 null입니다.")

                val items = parseItemsFromResponse(response)

                if (items.isNotEmpty()) {
                    println("검색된 책 목록:")
                    items.forEachIndexed { index, item ->
                        println("${index + 1}. 제목: ${item.getTextValue("title")}")
                        println("   작가: ${item.getTextValue("author")}")
                        println("   카테고리: ${item.getTextValue("categoryName")}")
                        println("   mallType: ${item.getTextValue("mallType")}")
                        println()
                    }
                } else {
                    println("카테고리 $categoryName 의 검색 결과가 없습니다.")
                }

                Thread.sleep(API_DELAY)
            }

        } catch (e: Exception) {
            handleApiError("카테고리 검색 테스트", e)
        }
    }

    @Test
    @DisplayName("알라딘 API 연결 테스트")
    fun testAladinConnection() {
        try {
            val url = buildSearchUrl(query = "test", maxResults = 1)
            val response = restTemplate.getForObject(url, String::class.java)

            if (response?.contains("item") == true) {
                println("✅ 알라딘 API 연결 성공!")

                val rootNode = objectMapper.readTree(response)
                println("API 버전: ${rootNode["version"]?.asText()}")
                println("총 결과 수: ${rootNode["totalResults"]?.asText()}")
            } else {
                println("❌ 알라딘 API 응답이 예상과 다릅니다.")
                println("응답: $response")
            }

        } catch (e: Exception) {
            println("❌ 알라딘 API 연결 실패: ${e.message}")

            println("\n가능한 원인:")
            listOf(
                "API 키가 올바르지 않음",
                "네트워크 연결 문제",
                "알라딘 API 서버 문제",
                "요청 형식 오류"
            ).forEach { println("- $it") }
        }
    }

    // Helper functions
    private fun buildSearchUrl(
        query: String,
        maxResults: Int,
        start: Int = 1,
        searchTarget: String = "Book",
        optResult: String = ""
    ): String = buildString {
        append("$BASE_URL/ItemSearch.aspx")
        append("?ttbkey=$API_KEY")
        append("&Query=$query")
        append("&QueryType=Title")
        append("&MaxResults=$maxResults")
        append("&start=$start")
        append("&SearchTarget=$searchTarget")
        append("&output=js")
        append("&Version=20131101")
        if (optResult.isNotEmpty()) {
            append("&OptResult=$optResult")
        }
    }

    private fun buildLookupUrl(isbn: String, optResult: String): String = buildString {
        append("$BASE_URL/ItemLookUp.aspx")
        append("?ttbkey=$API_KEY")
        append("&itemIdType=ISBN13")
        append("&ItemId=$isbn")
        append("&output=js")
        append("&Version=20131101")
        append("&OptResult=$optResult")
    }

    private fun buildCategoryListUrl(
        categoryId: Int,
        maxResults: Int,
        start: Int = 1,
        searchTarget: String = "Book"
    ): String = buildString {
        append("$BASE_URL/ItemList.aspx")
        append("?ttbkey=$API_KEY")
        append("&QueryType=ItemNewAll")
        append("&MaxResults=$maxResults")
        append("&start=$start")
        append("&SearchTarget=$searchTarget")
        append("&CategoryId=$categoryId")
        append("&output=js")
        append("&Version=20131101")
        append("&OptResult=authors")
    }

    private fun parseItemsFromResponse(response: String): List<JsonNode> {
        val rootNode = objectMapper.readTree(response)
        val itemsNode = rootNode["item"] ?: return emptyList()

        return if (itemsNode.isArray) {
            itemsNode.toList()
        } else {
            emptyList()
        }
    }

    private fun printBookInfo(index: Int, item: JsonNode) {
        println("$index. 제목: ${item.getTextValue("title")}")
        println("   작가: ${item.getTextValue("author")}")
        println("   출판사: ${item.getTextValue("publisher")}")
        println("   ISBN13: ${item.getTextValue("isbn13")}")
        println("   카테고리: ${item.getTextValue("categoryName")}")
        println("   몰타입: ${item.getTextValue("mallType")}")
        println("   표지: ${item.getTextValue("cover")}")
    }

    private fun printAuthorDetails(item: JsonNode) {
        val authorsNode = item["subInfo"]?.get("authors")

        if (authorsNode?.isArray == true && authorsNode.size() > 0) {
            println("   상세 작가 정보:")
            authorsNode.forEach { authorNode ->
                val authorName = authorNode.getTextValue("authorName")
                val authorType = authorNode.getTextValue("authorType")
                println("     - $authorName ($authorType)")
            }
        }
    }

    private fun printDetailedBookInfo(item: JsonNode) {
        println("\n조회된 책 정보:")

        val basicInfo = mapOf(
            "제목" to item.getTextValue("title"),
            "작가" to item.getTextValue("author"),
            "출판사" to item.getTextValue("publisher"),
            "ISBN13" to item.getTextValue("isbn13"),
            "총 페이지" to item.getTextValue("itemPage"),
            "출간일" to item.getTextValue("pubDate"),
            "평점" to item.getTextValue("customerReviewRank"),
            "몰타입" to item.getTextValue("mallType"),
            "표지" to item.getTextValue("cover")
        )

        basicInfo.forEach { (label, value) ->
            println("$label: $value")
        }

        val subInfo = item["subInfo"]
        if (subInfo != null) {
            println("\n부가 정보:")
            println("부제: ${subInfo.getTextValue("subTitle")}")
            println("쪽수: ${subInfo.getTextValue("itemPage")}")

            printDetailedAuthors(subInfo)
            printCategoryInfo(subInfo)
        }
    }

    private fun printDetailedAuthors(subInfo: JsonNode) {
        val authorsNode = subInfo["authors"]

        if (authorsNode?.isArray == true && authorsNode.size() > 0) {
            println("\n작가 상세 정보:")
            authorsNode.forEach { authorNode ->
                val name = authorNode.getTextValue("authorName")
                val type = authorNode.getTextValue("authorType")
                val typeDesc = authorNode.getTextValue("authorTypeDesc")
                println("- $name ($type: $typeDesc)")
            }
        }
    }

    private fun printCategoryInfo(subInfo: JsonNode) {
        val categoryIdListNode = subInfo["categoryIdList"]

        if (categoryIdListNode?.isArray == true && categoryIdListNode.size() > 0) {
            println("\n카테고리 정보:")
            categoryIdListNode.forEach { categoryNode ->
                val categoryInfo = categoryNode["categoryInfo"]
                if (categoryInfo != null) {
                    val id = categoryInfo.getTextValue("categoryId")
                    val name = categoryInfo.getTextValue("categoryName")
                    println("- $name (ID: $id)")
                }
            }
        }
    }

    private fun printSimpleBookInfo(item: JsonNode, expectedCategory: String) {
        println("제목: ${item.getTextValue("title")}")
        println("작가: ${item.getTextValue("author")}")
        println("mallType: ${item.getTextValue("mallType")}")
        println("카테고리: ${item.getTextValue("categoryName") ?: expectedCategory}")
    }

    private fun handleApiError(testName: String, e: Exception) {
        System.err.println("$testName 중 오류 발생: ${e.message}")
        e.printStackTrace()
    }

    private fun JsonNode.getTextValue(fieldName: String): String? =
        this[fieldName]?.takeUnless { it.isNull }?.asText()
}