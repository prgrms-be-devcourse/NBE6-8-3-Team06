package com.back.domain.bookmarks.dto

import com.back.domain.bookmarks.entity.Bookmark
import com.fasterxml.jackson.annotation.JsonUnwrapped

data class BookmarkModifyResponseDto(
    @field:JsonUnwrapped @param:JsonUnwrapped val bookmark: BookmarkDto
) {
    constructor(bookmark: Bookmark) : this(
        BookmarkDto(bookmark, null)
    )
}
