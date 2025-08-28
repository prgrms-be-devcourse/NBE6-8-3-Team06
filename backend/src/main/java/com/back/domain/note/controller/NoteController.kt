package com.back.domain.note.controller

import com.back.domain.note.dto.NoteDto
import com.back.domain.note.dto.NotePageDto
import com.back.domain.note.entity.Note
import com.back.domain.note.service.NoteService
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import lombok.RequiredArgsConstructor
import org.hibernate.validator.constraints.Length
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.util.stream.Collectors

@RestController
@RequiredArgsConstructor
@RequestMapping("bookmarks/{bookmarkId}/notes")
class NoteController {
    private val noteService: NoteService? = null
    private val rq: Rq? = null

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "노트 페이지 전체 조회")
    fun getItems(@PathVariable bookmarkId: Int): RsData<NotePageDto?> {
        val actor = rq!!.getActor()

        // 병합할때 시큐리티에서 따로 처리할 것
        if (actor == null) {
            return RsData<NotePageDto?>(
                "401-1",
                "로그인 후 이용해주세요"
            )
        }

        val bookmark = noteService!!.findBookmarkById(bookmarkId)

        noteService.checkNotePageCURD(bookmark!!, actor, "조회", "403-1") // 노트 페이지 조회 권한 확인

        val bookInfo = noteService.getBookInfo(bookmark)

        val notes = bookmark
            .notes
            .stream()
            .map<NoteDto?> { note: Note? -> NoteDto(note!!) }
            .collect(Collectors.toList())

        return RsData<NotePageDto?>(
            "200-1",
            "%d번 북마크의 노트 조회를 성공했습니다.".formatted(bookmarkId),
            NotePageDto(notes, bookInfo)
        )
    }


    @JvmRecord
    internal data class NoteWriteReqBody(
        val title: @NotBlank @Length(min = 1, max = 50) String?,
        val content: @NotBlank @Length(min = 1, max = 1000) String?,
        val page: String?
    )

    @PostMapping
    @Transactional
    @Operation(summary = "노트 작성")
    fun write(
        @PathVariable bookmarkId: Int,
        @RequestBody reqBody: @Valid NoteWriteReqBody
    ): RsData<NoteDto?> {
        val actor = rq!!.getActor()

        // 병합할때 시큐리티에서 따로 처리할 것
        if (actor == null) {
            return RsData<NoteDto?>(
                "401-1",
                "로그인 후 이용해주세요"
            )
        }

        val bookmark = noteService!!.findBookmarkById(bookmarkId)

        noteService.checkNotePageCURD(bookmark!!, actor, "작성", "403-2") // 노트 페이지 작성 권한 확인

        val note = noteService.write(bookmarkId, reqBody.title!!, reqBody.content!!, reqBody.page, actor)

        // 미리 db에 반영
        noteService.flush()

        return RsData<NoteDto?>(
            "201-1",
            "%d번 노트가 작성되었습니다.".formatted(note.id),
            NoteDto(note)
        )
    }


    @JvmRecord
    internal data class NoteModifyReqBody(
        val title: @NotBlank @Length(min = 1, max = 50) String?,
        val content: @NotBlank @Length(min = 1, max = 1000) String?,
        val page: String?
    )

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "노트 수정")
    fun modify(
        @PathVariable bookmarkId: Int,
        @PathVariable id: Int,
        @RequestBody reqBody: @Valid NoteModifyReqBody
    ): RsData<Void?> {
        val actor = rq!!.getActor()

        // 병합할때 시큐리티에서 따로 처리할 것
        if (actor == null) {
            return RsData<Void?>(
                "401-1",
                "로그인 후 이용해주세요"
            )
        }

        val bookmark = noteService!!.findBookmarkById(bookmarkId)

        noteService.checkNotePageCURD(bookmark!!, actor, "수정", "403-3") // 노트 페이지 작성 수정 확인

        val note = noteService.findNoteById(bookmark, id)

        noteService.modify(note, reqBody.title!!, reqBody.content!!, reqBody.page)

        return RsData<Void?>(
            "200-2",
            "%d번 노트가 수정되었습니다.".formatted(id)
        )
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "노트 삭제")
    fun delete(
        @PathVariable bookmarkId: Int,
        @PathVariable id: Int
    ): RsData<Void?> {
        val actor = rq!!.getActor()

        // 병합할때 시큐리티에서 따로 처리할 것
        if (actor == null) {
            return RsData<Void?>(
                "401-1",
                "로그인 후 이용해주세요"
            )
        }

        val bookmark = noteService!!.findBookmarkById(bookmarkId)

        noteService.checkNotePageCURD(bookmark!!, actor, "삭제", "403-4") // 노트 페이지 삭제 권한 확인

        val note = noteService.findNoteById(bookmark, id)

        noteService.delete(bookmark, note)

        return RsData<Void?>(
            "200-3",
            "%d번 노트가 삭제되었습니다.".formatted(id)
        )
    }
}
