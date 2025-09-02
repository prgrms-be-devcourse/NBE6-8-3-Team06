package com.back.domain.note.controller

import com.back.domain.note.entity.Note
import com.back.domain.note.service.NoteService
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc(addFilters = false) // security 필터 비활성(나중에 제거해야함)
class NoteControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var noteService: NoteService

    @Test
    @DisplayName("노트 로직 수행 전 로그인 안됨 확인")
    @Throws(Exception::class)
    fun t1() {
        val bookmarkId = 1

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/bookmarks/${bookmarkId}/notes")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(NoteController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getItems"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("로그인 후 이용해주세요"))
    }

    @Test
    @DisplayName("노트 페이지 전체 조회")
    @WithUserDetails("email1@naver.com")
    @Throws(Exception::class)
    fun t6() {
        val bookmarkId = 1

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/bookmarks/${bookmarkId}/notes")
            )
            .andDo(MockMvcResultHandlers.print())

        val bookmark = noteService.findBookmarkById(bookmarkId)
        val notes = bookmark.notes

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(NoteController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getItems"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("${bookmarkId}번 북마크의 노트 조회를 성공했습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.notes.length()").value(notes.size))

        for (i in notes.indices) {
            val note = notes[i]

            resultActions
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.notes[${i}].id").value(note.id))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.notes[${i}].createDate")
                        .value<String?>(Matchers.startsWith(note.createDate.toString().substring(0, 20)))
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.notes[${i}].modifyDate")
                        .value<String?>(Matchers.startsWith(note.modifyDate.toString().substring(0, 20)))
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.notes[${i}].title").value(note.title))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.notes[${i}].content").value(note.content))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.notes[${i}].page").value(note.page))
        }
    }

    @Test
    @DisplayName("노트 작성")
    @WithUserDetails("email1@naver.com")
    @Throws(Exception::class)
    fun t3() {
        val bookmarkId = 1

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/bookmarks/${bookmarkId}/notes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(
                        """
                                        {
                                            "title": "테스트 제목",
                                            "content": "테스트 내용",
                                            "page": "1"
                                        }
                                        
                                        """.trimIndent()
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        val bookmark = noteService.findBookmarkById(bookmarkId)
        val note: Note = bookmark.notes.last()

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(NoteController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("write"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("201-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("${note.id}번 노트가 작성되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(note.id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.createDate")
                    .value(Matchers.startsWith(note.createDate.toString().substring(0, 20)))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.modifyDate")
                    .value(Matchers.startsWith(note.modifyDate.toString().substring(0, 20)))
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("테스트 제목"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("테스트 내용"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.page").value("1"))
    }

    @Test
    @DisplayName("노트 작성 without permission")
    @WithUserDetails("email2@naver.com")
    @Throws(Exception::class)
    fun t7() {
        val bookmarkId = 1

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/bookmarks/${bookmarkId}/notes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(
                        """
                                        {
                                            "title": "테스트 제목",
                                            "content": "테스트 내용",
                                            "page": "1"
                                        }
                                        
                                        """.trimIndent()
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(NoteController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("write"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("403-2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("${bookmarkId}번 북마크의 노트 작성 권한이 없습니다."))
    }

    @Test
    @DisplayName("노트 수정")
    @WithUserDetails("email1@naver.com")
    @Throws(Exception::class)
    fun t4() {
        val bookmarkId = 1
        val id = 1

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.put("/bookmarks/${bookmarkId}/notes/${id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(
                        """
                                        {
                                            "title": "테스트 제목 new",
                                            "content": "테스트 내용 new",
                                            "page": "100"
                                        }
                                        
                                        """.trimIndent()
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(NoteController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("modify"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("${id}번 노트가 수정되었습니다."))
    }

    @Test
    @DisplayName("노트 수정 without permission")
    @WithUserDetails("email2@naver.com")
    @Throws(Exception::class)
    fun t9() {
        val bookmarkId = 1
        val id = 1

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.put("/bookmarks/${bookmarkId}/notes/${id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8)
                    .content(
                        """
                                        {
                                            "title": "테스트 제목",
                                            "content": "테스트 내용",
                                            "page": "1"
                                        }
                                        
                                        """.trimIndent()
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(NoteController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("modify"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("403-3"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("${bookmarkId}번 북마크의 노트 수정 권한이 없습니다."))
    }

    @Test
    @DisplayName("노트 삭제")
    @WithUserDetails("email1@naver.com")
    @Throws(Exception::class)
    fun t5() {
        val bookmarkId = 1
        val id = 1

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.delete("/bookmarks/${bookmarkId}/notes/${id}")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(NoteController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("delete"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-3"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("${id}번 노트가 삭제되었습니다."))
    }

    @Test
    @DisplayName("노트 삭제 without permission")
    @WithUserDetails("email2@naver.com")
    @Throws(Exception::class)
    fun t11() {
        val bookmarkId = 1
        val id = 1

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.delete("/bookmarks/${bookmarkId}/notes/${id}")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(NoteController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("delete"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("403-4"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("${bookmarkId}번 북마크의 노트 삭제 권한이 없습니다."))
    }
}
