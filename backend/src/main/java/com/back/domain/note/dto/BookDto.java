package com.back.domain.note.dto;

import org.springframework.lang.NonNull;

import java.util.List;

public record BookDto(
        @NonNull String imageUrl,
        @NonNull String title,
        @NonNull List<String> author,
        @NonNull String category
) {

}
