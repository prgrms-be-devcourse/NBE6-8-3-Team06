package com.back.domain.bookmarks.dto;

import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.note.dto.NoteDto;
import com.back.domain.review.review.entity.Review;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.List;

public record BookmarkDetailDto(
        @JsonUnwrapped
        BookmarkDto bookmarkDto,
        long readingDuration,
        List<NoteDto> notes
) {
    public BookmarkDetailDto(Bookmark bookmark, Review review) {
        this(
                new BookmarkDto(bookmark, review),
                bookmark.calculateReadingDuration(),
                bookmark.getNotes().stream().map(NoteDto::new).toList()
        );
    }
}
