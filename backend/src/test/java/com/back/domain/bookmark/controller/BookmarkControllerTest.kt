package com.back.domain.bookmark.controller

import com.back.domain.book.book.repository.BookRepository
import com.back.domain.bookmarks.controller.BookmarkController
import com.back.domain.bookmarks.dto.BookmarkDto
import com.back.domain.bookmarks.service.BookmarkService
import com.back.domain.member.member.service.MemberService
import jakarta.servlet.http.Cookie
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class BookmarkControllerTest(
    @Autowired
    private val mvc: MockMvc,
    @Autowired
    private val bookRepository: BookRepository,
    @Autowired
    private val bookmarkService: BookmarkService,
    @Autowired
    private val memberService: MemberService,
    @Autowired
    private val passwordEncoder: PasswordEncoder,
) {

    var id: Int = 0

    @BeforeEach
    fun setup() {
        var member = memberService.findByEmail("email@test.com").orElse(null)
        if (member == null) {
            member = memberService.join("testUser", "email@test.com", passwordEncoder.encode("password"))
        }

        bookmarkService.save(1, member)
        id = bookmarkService.getLatestBookmark(member).id
        println("id : " + id)
    }

    @Test
    @DisplayName("북마크 추가")
    @Throws(Exception::class)
    fun t1() {
        val member = memberService.findByEmail("email@test.com").get()
        val accessToken = memberService.geneAccessToken(member)
        val book = bookRepository.findById(3).get()
        val resultActions = mvc.perform(
            MockMvcRequestBuilders.post("/bookmarks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        {
                          "bookId" : ${book.id}
                        }
                        
                        """.trimIndent()
                )
                .cookie(Cookie("accessToken", accessToken))
        ).andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(BookmarkController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("addBookmark"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("201-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("${book.id} 번 책이 내 책 목록에 추가되었습니다."))
    }

    @Test
    @DisplayName("북마크 단건 조회")
    @Throws(Exception::class)
    fun t2() {
        val member = memberService.findByEmail("email@test.com").get()
        val accessToken = memberService.geneAccessToken(member)
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/bookmarks/" + id)
                    .cookie(Cookie("accessToken", accessToken))
            )
            .andDo(MockMvcResultHandlers.print())
        val bookmarkDto = bookmarkService.getBookmarkById(member, id)

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(BookmarkController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getBookmark"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("${bookmarkDto.bookmarkDto.id}번 조회 성공"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.bookId").value(bookmarkDto.bookmarkDto.bookId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.readState").value(bookmarkDto.bookmarkDto.readState))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.readPage").value(bookmarkDto.bookmarkDto.readPage))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.createDate")
                    .value(Matchers.startsWith(bookmarkDto.bookmarkDto.createDate.toString().substring(0, 18)))
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.startReadDate").isEmpty())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.endReadDate").isEmpty())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.readingDuration").value(bookmarkDto.readingDuration))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.book.id").value(bookmarkDto.bookmarkDto.bookId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.book.isbn13").value(bookmarkDto.bookmarkDto.book.isbn13))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.book.title").value(bookmarkDto.bookmarkDto.book.title))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.book.imageUrl").value(bookmarkDto.bookmarkDto.book.imageUrl)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.book.publisher").value(bookmarkDto.bookmarkDto.book.publisher)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.book.totalPage").value(bookmarkDto.bookmarkDto.book.totalPage)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.book.avgRate").value(bookmarkDto.bookmarkDto.book.avgRate)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.book.category").value(bookmarkDto.bookmarkDto.book.category)
            ) //                .andExpect(jsonPath("$.data.book.authors").value(bookmarkDto.bookmarkDto().book().authors()))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.book.publishDate").value(
                    Matchers.startsWith(
                        bookmarkDto.bookmarkDto.book.publishDate.toString().substring(0, 16)
                    )
                )
            )
    }

    @Test
    @DisplayName("결제 단건 실패")
    @Throws(Exception::class)
    fun t3() {
        val id = Int.MAX_VALUE
        val member = memberService.findByEmail("email@test.com").get()
        val accessToken = memberService.geneAccessToken(member)
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/bookmarks/" + id)
                    .cookie(Cookie("accessToken", accessToken))
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(BookmarkController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getBookmark"))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("404-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("${id}번 데이터가 없습니다."))
    }

    @Test
    @DisplayName("북마크 다건 조회 - 목록")
    @Throws(Exception::class)
    fun t4() {
        val member = memberService.findByEmail("email@test.com").get()
        val accessToken = memberService.geneAccessToken(member)
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/bookmarks/list")
                    .cookie(Cookie("accessToken", accessToken))
            )
            .andDo(MockMvcResultHandlers.print())

        val bookmarksDtoList = bookmarkService.toList(member)

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(BookmarkController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getBookmarksToList"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(bookmarksDtoList.size))

        for (i in bookmarksDtoList.indices) {
            val bookmarksDto = bookmarksDtoList.get(i)
            resultActions
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[${i}].id").value(bookmarksDto.id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[$i].bookId").value(bookmarksDto.bookId))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data[$i].readState").value(bookmarksDto.readState)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data[$i].readPage").value(bookmarksDto.readPage)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data[$i].readingRate")
                        .value(bookmarksDto.readingRate)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data[$i].createDate")
                        .value(Matchers.startsWith(bookmarksDto.createDate.toString().substring(0, 18)))
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data[$i].book.id").value(bookmarksDto.book.id)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data[$i].book.isbn13")
                        .value(bookmarksDto.book.isbn13)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data[$i].book.title").value(bookmarksDto.book.title)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data[$i].book.imageUrl")
                        .value(bookmarksDto.book.imageUrl)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data[$i].book.publisher")
                        .value(bookmarksDto.book.publisher)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data[$i].book.totalPage")
                        .value(bookmarksDto.book.totalPage)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data[$i].book.avgRate")
                        .value(bookmarksDto.book.avgRate)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data[$i].book.category")
                        .value(bookmarksDto.book.category)
                ) //                .andExpect(jsonPath("$.data[%d].book.authors".formatted(i)).value(bookmarksDto.book().authors()))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data[$i].book.publishDate")
                        .value(Matchers.startsWith(bookmarksDto.book.publishDate.toString().substring(0, 16)))
                )
        }
    }

    @Test
    @DisplayName("북마크 다건 조회 - 페이지")
    @Throws(Exception::class)
    fun t5() {
        val member = memberService.findByEmail("email@test.com").get()
        val accessToken = memberService.geneAccessToken(member)
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/bookmarks")
                    .cookie(Cookie("accessToken", accessToken))
            )
            .andDo(MockMvcResultHandlers.print())

        val bookmarksDtoPage: Page<BookmarkDto> = bookmarkService.toPage(
            member,
            PageRequest.of(0, 10, Sort.by("createDate").descending()),
            null,
            null,
            null
        )

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(BookmarkController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getBookmarksToPage"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.data.length()").value(bookmarksDtoPage.getContent().size))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.pageNumber").value(bookmarksDtoPage.getNumber()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.pageSize").value(bookmarksDtoPage.getSize()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.totalPages").value(bookmarksDtoPage.getTotalPages()))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.totalElements").value(bookmarksDtoPage.getTotalElements())
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.isLast").value(bookmarksDtoPage.isLast()))

        for (i in bookmarksDtoPage.getContent().indices) {
            val bookmarksDto = bookmarksDtoPage.getContent().get(i)
            resultActions
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.data[$i].id").value(bookmarksDto.id))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.data[$i].bookId").value(bookmarksDto.bookId)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.data[$i].readState")
                        .value(bookmarksDto.readState)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.data[$i].readPage").value(bookmarksDto.readPage)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.data[$i].readingRate")
                        .value(bookmarksDto.readingRate)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.data[$i].createDate")
                        .value(Matchers.startsWith(bookmarksDto.createDate.toString().substring(0, 18)))
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.data[$i].book.id").value(bookmarksDto.book.id)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.data[$i].book.isbn13")
                        .value(bookmarksDto.book.isbn13)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.data[$i].book.title")
                        .value(bookmarksDto.book.title)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.data[$i].book.imageUrl")
                        .value(bookmarksDto.book.imageUrl)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.data[$i].book.publisher")
                        .value(bookmarksDto.book.publisher)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.data[$i].book.totalPage")
                        .value(bookmarksDto.book.totalPage)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.data[$i].book.avgRate")
                        .value(bookmarksDto.book.avgRate)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.data[$i].book.category")
                        .value(bookmarksDto.book.category)
                )
            //                .andExpect(jsonPath("$.data.data[%d].book.authors".formatted(i)).value(bookmarksDto.book().authors()))
//                    .andExpect(jsonPath("$.data.data[%d].book.publishDate".formatted(i)).value(Matchers.startsWith(bookmarksDto.book().publishDate().toString().substring(0,16))));
        }
    }

    @Test
    @DisplayName("북마크 수정")
    @Throws(Exception::class)
    fun t6() {
        val member = memberService.findByEmail("email@test.com").get()
        val accessToken = memberService.geneAccessToken(member)
        val resultActions = mvc.perform(
            MockMvcRequestBuilders.put("/bookmarks/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        {
                          "readState" : "READING",
                          "startReadDate" : "2025-07-22T10:00:00",
                          "readPage" : 100
                        }
                        
                        """.trimIndent()
                )
                .cookie(Cookie("accessToken", accessToken))
        ).andDo(MockMvcResultHandlers.print())

        val bookmark = bookmarkService.findById(id)
        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(BookmarkController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("modifyBookmark"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("${id}번 북마크가 수정되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.bookId").value(bookmark.book.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.readState").value(bookmark.readState.toString()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.readPage").value(bookmark.readPage))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.readingRate").value(bookmark.calculateReadingRate()))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.createDate")
                    .value(Matchers.startsWith(bookmark.createDate.toString().substring(0, 16)))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.startReadDate")
                    .value(Matchers.startsWith(bookmark.startReadDate.toString().substring(0, 16)))
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.endReadDate").isEmpty())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.book.id").value(bookmark.book.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.book.isbn13").value(bookmark.book.getIsbn13()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.book.title").value(bookmark.book.getTitle()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.book.imageUrl").value(bookmark.book.getImageUrl()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.book.publisher").value(bookmark.book.getPublisher()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.book.totalPage").value(bookmark.book.getTotalPage()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.book.avgRate").value(bookmark.book.getAvgRate()))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.book.category").value(bookmark.book.getCategory().getName())
            ) //                .andExpect(jsonPath("$.data.book.authors").value(bookmark.book().authors()))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.book.publishDate")
                    .value(Matchers.startsWith(bookmark.book.getPublishedDate().toString().substring(0, 16)))
            )
    }

    @Test
    @DisplayName("북마크 삭제")
    @Throws(Exception::class)
    fun t7() {
        val member = memberService.findByEmail("email@test.com").get()
        val accessToken = memberService.geneAccessToken(member)
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.delete("/bookmarks/" + id)
                    .cookie(Cookie("accessToken", accessToken))
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(BookmarkController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("deleteBookmark"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("${id} 북마크가 삭제되었습니다."))
    }

    @Test
    @DisplayName("북마크 내책 목록 상태 조회")
    @Throws(Exception::class)
    fun t8() {
        val member = memberService.findByEmail("email@test.com").get()
        val accessToken = memberService.geneAccessToken(member)
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/bookmarks/read-states")
                    .cookie(Cookie("accessToken", accessToken))
            )
            .andDo(MockMvcResultHandlers.print())

        val bookmarkReadStatesDto = bookmarkService.getReadStatesCount(member, null, null, null)

        val readState = bookmarkReadStatesDto.readState
        if (readState != null) {
            resultActions
                .andExpect(MockMvcResultMatchers.handler().handlerType(BookmarkController::class.java))
                .andExpect(MockMvcResultMatchers.handler().methodName("getBookmarkReadStates"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("조회 성공"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.totalCount").value(bookmarkReadStatesDto.totalCount))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.avgRate").value(bookmarkReadStatesDto.avgRate))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.readState.read").value(readState.READ)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.readState.reading")
                        .value(readState.READING)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.readState.wish").value(readState.WISH)
                )
        }
    }
}
