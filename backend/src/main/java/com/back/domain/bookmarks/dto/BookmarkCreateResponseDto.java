package com.back.domain.bookmarks.dto;

import com.back.domain.bookmarks.entity.Bookmark;

public record BookmarkCreateResponseDto(
        int id
) {
    public BookmarkCreateResponseDto(Bookmark bookmark) {
        this(bookmark.getId());
    }
}
