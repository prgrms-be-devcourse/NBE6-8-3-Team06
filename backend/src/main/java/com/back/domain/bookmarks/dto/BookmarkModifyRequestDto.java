package com.back.domain.bookmarks.dto;

import java.time.LocalDateTime;

public record BookmarkModifyRequestDto(
        String readState,
        LocalDateTime startReadDate,
        LocalDateTime endReadDate,
        int readPage
) {
}
