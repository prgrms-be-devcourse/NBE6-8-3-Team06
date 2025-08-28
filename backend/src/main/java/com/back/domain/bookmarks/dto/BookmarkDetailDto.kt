package com.back.domain.bookmarks.dto

import com.back.domain.bookmarks.entity.Bookmark
import com.back.domain.note.dto.NoteDto
import com.back.domain.note.entity.Note
import com.back.domain.review.review.entity.Review
import com.fasterxml.jackson.annotation.JsonUnwrapped

data class BookmarkDetailDto(
    @field:JsonUnwrapped @param:JsonUnwrapped val bookmarkDto: BookmarkDto,
    val readingDuration: Long,
    val notes: MutableList<NoteDto>
) {
    constructor(bookmark: Bookmark, review: Review?) : this(
        BookmarkDto(bookmark, review),
        bookmark.calculateReadingDuration(),
        bookmark.notes.stream().map<NoteDto> { note: Note -> NoteDto(note) }.toList()
    )
}
