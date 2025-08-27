package com.back.domain.note.controller;

import com.back.domain.book.book.entity.Book;
import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.note.entity.Note;
import com.back.domain.note.service.NoteService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc(addFilters = false) // security 필터 비활성(나중에 제거해야함)
public class NoteControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private NoteService noteService;

    @Autowired
    private MemberRepository memberRepository;

//    @Test
//    @DisplayName("노트 다건 조회")
//    void t2() throws Exception {
//        int bookmarkId = 1;
//
//        ResultActions resultActions = mvc
//                .perform(
//                        get("/bookmarks/%d/notes".formatted(bookmarkId))
//                )
//                .andDo(print());
//
//        Bookmark bookmark = noteService.findBookmarkById(bookmarkId).get();
//        List<Note> notes = bookmark.getNotes();
//
//        resultActions
//                .andExpect(handler().handlerType(NoteController.class))
//                .andExpect(handler().methodName("getItems"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(notes.size()));
//
//        for (int i = 0; i < notes.size(); i++) {
//            Note note = notes.get(i);
//
//            resultActions
//                    .andExpect(jsonPath("$[%d].id".formatted(i)).value(note.getId()))
//                    .andExpect(jsonPath("$[%d].createDate".formatted(i)).value(Matchers.startsWith(note.getCreateDate().toString().substring(0, 20))))
//                    .andExpect(jsonPath("$[%d].modifyDate".formatted(i)).value(Matchers.startsWith(note.getModifyDate().toString().substring(0, 20))))
//                    .andExpect(jsonPath("$[%d].title".formatted(i)).value(note.getTitle()))
//                    .andExpect(jsonPath("$[%d].content".formatted(i)).value(note.getContent()))
//                    .andExpect(jsonPath("$[%d].page".formatted(i)).value(note.getPage()));
//        }
//    }
    @Test
    @DisplayName("노트 로직 수행 전 로그인 안됨 확인")
    void t1() throws Exception {
        int bookmarkId = 1;

        ResultActions resultActions = mvc
                .perform(
                        get("/bookmarks/%d/notes".formatted(bookmarkId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(NoteController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("로그인 후 이용해주세요"));
    }

    @Test
    @DisplayName("노트 페이지 전체 조회")
    @WithUserDetails("email1@naver.com")
    void t6() throws Exception {
        int bookmarkId = 1;

        ResultActions resultActions = mvc
                .perform(
                        get("/bookmarks/%d/notes".formatted(bookmarkId))
                )
                .andDo(print());

        Bookmark bookmark = noteService.findBookmarkById(bookmarkId).get();
        Book book = bookmark.getBook();
        List<Note> notes = bookmark.getNotes();

        resultActions
                .andExpect(handler().handlerType(NoteController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 북마크의 노트 조회를 성공했습니다.".formatted(bookmarkId)))
                .andExpect(jsonPath("$.data.imageUrl").value(book.getImageUrl()))
                .andExpect(jsonPath("$.data.title").value(book.getTitle()))
                .andExpect(jsonPath("$.data.notes.length()").value(notes.size()));
//                .andExpect(jsonPath("$.author").value(book.getAuthors()))

        for (int i = 0; i < notes.size(); i++) {
            Note note = notes.get(i);

            resultActions
                    .andExpect(jsonPath("$.data.notes[%d].id".formatted(i)).value(note.getId()))
                    .andExpect(jsonPath("$.data.notes[%d].createDate".formatted(i)).value(Matchers.startsWith(note.getCreateDate().toString().substring(0, 20))))
                    .andExpect(jsonPath("$.data.notes[%d].modifyDate".formatted(i)).value(Matchers.startsWith(note.getModifyDate().toString().substring(0, 20))))
                    .andExpect(jsonPath("$.data.notes[%d].title".formatted(i)).value(note.getTitle()))
                    .andExpect(jsonPath("$.data.notes[%d].content".formatted(i)).value(note.getContent()))
                    .andExpect(jsonPath("$.data.notes[%d].page".formatted(i)).value(note.getPage()));
        }
    }

    @Test
    @DisplayName("노트 작성")
    @WithUserDetails("email1@naver.com")
    void t3() throws Exception {
        int bookmarkId = 1;

        ResultActions resultActions = mvc
                .perform(
                        post("/bookmarks/%d/notes".formatted(bookmarkId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .content("""
                                        {
                                            "title": "테스트 제목",
                                            "content": "테스트 내용",
                                            "page": "1"
                                        }
                                        """)
                )
                .andDo(print());

        Bookmark bookmark = noteService.findBookmarkById(bookmarkId).get();
        Note note = bookmark.getNotes().getLast();

        resultActions
                .andExpect(handler().handlerType(NoteController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%d번 노트가 작성되었습니다.".formatted(note.getId())))
                .andExpect(jsonPath("$.data.id").value(note.getId()))
                .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(note.getCreateDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.data.modifyDate").value(Matchers.startsWith(note.getModifyDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.data.title").value("테스트 제목"))
                .andExpect(jsonPath("$.data.content").value("테스트 내용"))
                .andExpect(jsonPath("$.data.page").value("1"));
    }

    @Test
    @DisplayName("노트 작성 without permission")
    @WithUserDetails("email2@naver.com")
    void t7() throws Exception {
        int bookmarkId = 1;

        ResultActions resultActions = mvc
                .perform(
                        post("/bookmarks/%d/notes".formatted(bookmarkId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .content("""
                                        {
                                            "title": "테스트 제목",
                                            "content": "테스트 내용",
                                            "page": "1"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(NoteController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.msg").value("%d번 북마크의 노트 작성 권한이 없습니다.".formatted(bookmarkId)));
    }

    @Test
    @DisplayName("노트 수정")
    @WithUserDetails("email1@naver.com")
    void t4() throws Exception {
        int bookmarkId = 1;
        int id = 1;

        ResultActions resultActions = mvc
                .perform(
                        put("/bookmarks/%d/notes/%d".formatted(bookmarkId, id))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .content("""
                                        {
                                            "title": "테스트 제목 new",
                                            "content": "테스트 내용 new",
                                            "page": "100"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(NoteController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-2"))
                .andExpect(jsonPath("$.msg").value("%d번 노트가 수정되었습니다.".formatted(id)));
    }

    @Test
    @DisplayName("노트 수정 without permission")
    @WithUserDetails("email2@naver.com")
    void t9() throws Exception {
        int bookmarkId = 1;
        int id = 1;

        ResultActions resultActions = mvc
                .perform(
                        put("/bookmarks/%d/notes/%d".formatted(bookmarkId, id))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .content("""
                                        {
                                            "title": "테스트 제목",
                                            "content": "테스트 내용",
                                            "page": "1"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(NoteController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.msg").value("%d번 북마크의 노트 수정 권한이 없습니다.".formatted(bookmarkId)));
    }

    @Test
    @DisplayName("노트 삭제")
    @WithUserDetails("email1@naver.com")
    void t5() throws Exception {
        int bookmarkId = 1;
        int id = 1;

        ResultActions resultActions = mvc
                .perform(
                        delete("/bookmarks/%d/notes/%d".formatted(bookmarkId, id))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(NoteController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-3"))
                .andExpect(jsonPath("$.msg").value("%d번 노트가 삭제되었습니다.".formatted(id)));
    }

    @Test
    @DisplayName("노트 삭제 without permission")
    @WithUserDetails("email2@naver.com")
    void t11() throws Exception {
        int bookmarkId = 1;
        int id = 1;

        ResultActions resultActions = mvc
                .perform(
                        delete("/bookmarks/%d/notes/%d".formatted(bookmarkId, id))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(NoteController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.msg").value("%d번 북마크의 노트 삭제 권한이 없습니다.".formatted(bookmarkId)));
    }
}
