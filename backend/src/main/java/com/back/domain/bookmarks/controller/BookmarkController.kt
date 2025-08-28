package com.back.domain.bookmarks.controller

import com.back.domain.bookmarks.dto.*
import com.back.domain.bookmarks.service.BookmarkService
import com.back.global.dto.PageResponseDto
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/bookmarks")
class BookmarkController(
    private val bookmarkService: BookmarkService,
    private val rq: Rq
) {


    /**
     * 북마크 추가
     * @param bookmarkCreateRequestDto {"bookId":  number}
     * @return {"resultCode": string, "msg": string, "data": null}
     */
    @PostMapping
    @Transactional
    fun addBookmark(@RequestBody bookmarkCreateRequestDto: BookmarkCreateRequestDto): RsData<Void> {
        val member = rq.getActor()
        bookmarkService.save(bookmarkCreateRequestDto.bookId, member)
        return RsData(
            "201-1",
            "${bookmarkCreateRequestDto.bookId} 번 책이 내 책 목록에 추가되었습니다.",
            null
        )
    }

    /**
     * 북마크 단건 상세 조회
     * @param id
     * @return {"resultCode": string, "msg": string, "data": {bookmarkDetailDto}}
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    fun getBookmark(@PathVariable id: Int): RsData<BookmarkDetailDto> {
        val member = rq.getActor()
        val bookmark = bookmarkService.getBookmarkById(member, id)
        return RsData("200-1", "${id}번 조회 성공", bookmark)
    }

    @get:Transactional(readOnly = true)
    @get:GetMapping("/list")
    val bookmarksToList: RsData<MutableList<BookmarkDto>>
        /**
         * 북마크 목록 전체 조회
         * @return {"resultCode": string, "msg": string, "data": {[bookmarkDto]}}
         */
        get() {
            val member = rq.getActor()
            val bookmarks: MutableList<BookmarkDto> = bookmarkService.toList(member)
            if (bookmarks.isEmpty()) {
                return RsData("404-1", "데이터가 없습니다.", null)
            }
            return RsData(
                "200-1",
                "${bookmarks.size}개 조회 성공",
                bookmarks
            )
        }

    /**
     * 북마크 목록 조회 - 페이지
     * @return {"resultCode": string, "msg": string, "data": {PageResponseDto : {pageInfo, data:{data:[bookmarkDto]}}}}
     */
    @GetMapping("")
    @Transactional(readOnly = true)
    fun getBookmarksToPage(
        @PageableDefault(page = 0, size = 10, sort = ["createDate"], direction = Sort.Direction.DESC)
        pageable: Pageable,
        @RequestParam(value = "category", required = false) category: String?,
        @RequestParam(value = "readState", required = false) read_state: String?,
        @RequestParam(value = "keyword", required = false) keyword: String?
    ): RsData<PageResponseDto<BookmarkDto>> {
        val member = rq.getActor()
        val bookmarkDtoPage = bookmarkService.toPage(member, pageable, category, read_state, keyword)
        if (bookmarkDtoPage.isEmpty()) {
            return RsData("404-1", "데이터가 없습니다.", null)
        }
        return RsData(
            "200-1",
            "${pageable.pageNumber}번 페이지 조회 성공",
            PageResponseDto<BookmarkDto>(bookmarkDtoPage)
        )
    }

    /**
     * 북마크 수정
     * @param id
     * @param bookmarkModifyRequestDto
     * @return {"resultCode": string, "msg": string, "data": {bookmarkDetailDto}}
     */
    @PutMapping("/{id}")
    @Transactional
    fun modifyBookmark(
        @PathVariable id: Int,
        @RequestBody bookmarkModifyRequestDto: BookmarkModifyRequestDto
    ): RsData<BookmarkModifyResponseDto> {
        val member = rq.getActor()
        val bookmark = bookmarkService.modifyBookmark(
            member,
            id,
            bookmarkModifyRequestDto.readState,
            bookmarkModifyRequestDto.startReadDate,
            bookmarkModifyRequestDto.endReadDate,
            bookmarkModifyRequestDto.readPage
        )
        return RsData(
            "200-1",
            "${id}번 북마크가 수정되었습니다.",
            bookmark
        )
    }

    /**
     * 북마크 삭제
     * @param id
     * @return {"resultCode": string, "msg": string, "data": null}
     */
    @DeleteMapping("/{id}")
    @Transactional
    fun deleteBookmark(@PathVariable id: Int): RsData<Void> {
        val member = rq.getActor()
        bookmarkService.deleteBookmark(member, id)
        return RsData("200-1", "${id} 북마크가 삭제되었습니다.", null)
    }

    /**
     * 북마크 상태 카운트 조회
     * @return {"resultCode": string, "msg": string, "data": {bookmarkReadStateDto}}
     */
    @GetMapping("/read-states")
    @Transactional(readOnly = true)
    fun getBookmarkReadStates(
        @RequestParam(value = "category", required = false) category: String?,
        @RequestParam(value = "readState", required = false) read_state: String?,
        @RequestParam(value = "keyword", required = false) keyword: String?
    ): RsData<BookmarkReadStatesDto?> {
        val member = rq.getActor()
        val bookmarkReadStatesDto = bookmarkService.getReadStatesCount(member, category, read_state, keyword)
        return RsData<BookmarkReadStatesDto?>("200-1", "조회 성공", bookmarkReadStatesDto)
    }
}
