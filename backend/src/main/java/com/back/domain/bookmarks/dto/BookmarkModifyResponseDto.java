package com.back.domain.bookmarks.dto;

import com.back.domain.bookmarks.entity.Bookmark;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record BookmarkModifyResponseDto(
        @JsonUnwrapped
        BookmarkDto bookmark
) {
    public BookmarkModifyResponseDto(Bookmark bookmark){
        this(
                new BookmarkDto(bookmark, null)
        );
    }
}
