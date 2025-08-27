package com.back.domain.note.controller;

import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.note.entity.Note;
import com.back.domain.note.service.NoteService;
import com.back.global.rq.Rq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteController.class) // NoteController만 로딩
@AutoConfigureMockMvc(addFilters = false) // security 필터 비활성(나중에 제거해야함)
public class NoteMockTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private NoteService noteService;

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private Rq rq;

    @Test
    @DisplayName("노트 단건 조회 - Mock 테스트")
    void getSingleNote() throws Exception {
        // given
        int bookmarkId = 1;
        int noteId = 1;

        // 가짜 Bookmark 데이터 생성
        Bookmark bookmark = new Bookmark(null, null);

        // 가짜 Note 데이터 생성
        Note note = new Note("제목", "내용", "10", bookmark, null);

        // noteService mock 정의
        given(noteService.findBookmarkById(bookmarkId)).willReturn(Optional.of(bookmark));
        given(noteService.findNoteById(bookmark, noteId)).willReturn(Optional.of(note));

        // when
        ResultActions resultActions = mvc
                .perform(delete("/bookmarks/{bookmarkId}/notes/{noteId}", bookmarkId, noteId))
                .andDo(print());

        // then
        resultActions
                .andExpect(handler().handlerType(NoteController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(note.getId()))
//                .andExpect(jsonPath("$.createDate").value(Matchers.startsWith(note.CreateDateParsing(note.getCreateDate()))))
//                .andExpect(jsonPath("$.modifyDate").value(Matchers.startsWith(note.UpdateDateParsing(note.getModifyDate()))))
                .andExpect(jsonPath("$.title").value(note.getTitle()))
                .andExpect(jsonPath("$.content").value(note.getContent()))
                .andExpect(jsonPath("$.page").value(note.getPage()));
    }
}
