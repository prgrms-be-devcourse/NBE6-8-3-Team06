package com.back.domain.bookmarks.dto;

import jakarta.validation.constraints.NotNull;

public record BookmarkCreateRequestDto(
        @NotNull
        int bookId
) {
}
