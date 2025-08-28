package com.back.domain.bookmarks.dto

import com.back.domain.bookmarks.entity.Bookmark


data class BookmarkCreateResponseDto(
    val id: Int
) {
    constructor(bookmark: Bookmark) : this(bookmark.id)
}
