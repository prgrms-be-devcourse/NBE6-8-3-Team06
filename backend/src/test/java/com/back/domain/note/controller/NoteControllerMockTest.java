package com.back.domain.note.controller;

import com.back.domain.book.book.entity.Book;
import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.note.entity.Note;
import com.back.domain.note.service.NoteService;
import com.back.global.rq.Rq;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("노트 컨트롤러 Mock 테스트")
public class NoteControllerMockTest {

    @InjectMocks
    private NoteController noteController;

    @Mock
    private NoteService noteService;

    @Mock
    private Rq rq;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(noteController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("노트 페이지 전체 조회 - Mock 방식")
    void getNotesByBookmark_Mock() throws Exception {
        // Given
        int bookmarkId = 1;

        // 가짜 Book 데이터
        Book book = new Book("테스트 책", "테스트 작가", null);

        // 가짜 Bookmark 데이터
        Bookmark bookmark = new Bookmark(book, null);

        // 가짜 Note 데이터들
        List<Note> notes = Arrays.asList(
                new Note("노트제목1", "노트내용1", "10", bookmark, null),
                new Note("노트제목2", "노트내용2", "25", bookmark, null)
        );

        // Mock 동작 정의
        when(noteService.findBookmarkById(bookmarkId)).thenReturn(Optional.of(bookmark));

        // When
        ResultActions resultActions = mockMvc
                .perform(get("/bookmarks/%d/notes".formatted(bookmarkId)))
                .andDo(print());

        // Then
        resultActions
                .andExpect(handler().handlerType(NoteController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 북마크의 노트 조회를 성공했습니다.".formatted(bookmarkId)))
                .andExpect(jsonPath("$.data.bookInfo.imageUrl").value(book.getImageUrl()))
                .andExpect(jsonPath("$.data.bookInfo.title").value(book.getTitle()))
                .andExpect(jsonPath("$.data.notes.length()").value(notes.size()));

        // 각 노트 검증
        for (int i = 0; i < notes.size(); i++) {
            Note note = notes.get(i);

            resultActions
                    .andExpect(jsonPath("$.data.notes[%d].id".formatted(i)).value(note.getId()))
                    .andExpect(jsonPath("$.data.notes[%d].title".formatted(i)).value(note.getTitle()))
                    .andExpect(jsonPath("$.data.notes[%d].content".formatted(i)).value(note.getContent()))
                    .andExpect(jsonPath("$.data.notes[%d].page".formatted(i)).value(note.getPage()));
        }

        // Mock 호출 검증
        verify(noteService, times(1)).findBookmarkById(bookmarkId);
    }
}