package com.back.domain.bookmarks.dto;

public record BookmarkReadStatesDto(
        long totalCount,
        double avgRate,
        ReadStateCount readState
) {

}
