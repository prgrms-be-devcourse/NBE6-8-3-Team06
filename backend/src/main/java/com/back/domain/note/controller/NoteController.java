package com.back.domain.note.controller;

import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.member.member.entity.Member;
import com.back.domain.note.dto.BookDto;
import com.back.domain.note.dto.NoteDto;
import com.back.domain.note.dto.NotePageDto;
import com.back.domain.note.entity.Note;
import com.back.domain.note.service.NoteService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("bookmarks/{bookmarkId}/notes")
public class NoteController {
    private final NoteService noteService;
    private final Rq rq;

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "노트 페이지 전체 조회")
    public RsData<NotePageDto> getItems(@PathVariable int bookmarkId) {
        Member actor = rq.getActor();

        // 병합할때 시큐리티에서 따로 처리할 것
        if (actor == null) {
            return new RsData<>(
                    "401-1",
                    "로그인 후 이용해주세요"
            );
        }

        Bookmark bookmark = noteService.findBookmarkById(bookmarkId).get();

        noteService.checkNotePageCURD(bookmark, actor, "조회", "403-1"); // 노트 페이지 조회 권한 확인

        BookDto bookInfo = noteService.getBookInfo(bookmark);

        List<NoteDto> notes = bookmark
                .getNotes()
                .stream()
                .map(note -> new NoteDto(note))
                .collect(Collectors.toList());

        return new RsData<>(
                "200-1",
                "%d번 북마크의 노트 조회를 성공했습니다.".formatted(bookmarkId),
                new NotePageDto(notes, bookInfo)
        );
    }


    record NoteWriteReqBody(
            @NotBlank
            @Length(min = 1, max = 50)
            String title,
            @NotBlank
            @Length(min = 1, max = 1000)
            String content,
            String page
    ) {
    }

    @PostMapping
    @Transactional
    @Operation(summary = "노트 작성")
    public RsData<NoteDto> write(
            @PathVariable int bookmarkId,
            @Valid @RequestBody NoteWriteReqBody reqBody
    ) {
        Member actor = rq.getActor();

        // 병합할때 시큐리티에서 따로 처리할 것
        if (actor == null) {
            return new RsData<>(
                    "401-1",
                    "로그인 후 이용해주세요"
            );
        }

        Bookmark bookmark = noteService.findBookmarkById(bookmarkId).get();

        noteService.checkNotePageCURD(bookmark, actor, "작성", "403-2"); // 노트 페이지 작성 권한 확인

        Note note = noteService.write(bookmarkId, reqBody.title, reqBody.content, reqBody.page, actor);

        // 미리 db에 반영
        noteService.flush();

        return new RsData<>(
                "201-1",
                "%d번 노트가 작성되었습니다.".formatted(note.getId()),
                new NoteDto(note)
        );
    }


    record NoteModifyReqBody(
            @NotBlank
            @Length(min = 1, max = 50)
            String title,
            @NotBlank
            @Length(min = 1, max = 1000)
            String content,
            String page
    ) {
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "노트 수정")
    public RsData<Void> modify(
            @PathVariable int bookmarkId,
            @PathVariable int id,
            @Valid @RequestBody NoteModifyReqBody reqBody
    ) {
        Member actor = rq.getActor();

        // 병합할때 시큐리티에서 따로 처리할 것
        if (actor == null) {
            return new RsData<>(
                    "401-1",
                    "로그인 후 이용해주세요"
            );
        }

        Bookmark bookmark = noteService.findBookmarkById(bookmarkId).get();

        noteService.checkNotePageCURD(bookmark, actor, "수정", "403-3"); // 노트 페이지 작성 수정 확인

        Note note = noteService.findNoteById(bookmark, id).get();

        noteService.modify(note, reqBody.title, reqBody.content, reqBody.page);

        return new RsData<>(
                "200-2",
                "%d번 노트가 수정되었습니다.".formatted(id)
        );
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "노트 삭제")
    public RsData<Void> delete(
            @PathVariable int bookmarkId,
            @PathVariable int id
    ) {
        Member actor = rq.getActor();

        // 병합할때 시큐리티에서 따로 처리할 것
        if (actor == null) {
            return new RsData<>(
                    "401-1",
                    "로그인 후 이용해주세요"
            );
        }

        Bookmark bookmark = noteService.findBookmarkById(bookmarkId).get();

        noteService.checkNotePageCURD(bookmark, actor, "삭제", "403-4"); // 노트 페이지 삭제 권한 확인

        Note note = noteService.findNoteById(bookmark, id).get();

        noteService.delete(bookmark, note);

        return new RsData<>(
                "200-3",
                "%d번 노트가 삭제되었습니다.".formatted(id)
        );
    }
}
