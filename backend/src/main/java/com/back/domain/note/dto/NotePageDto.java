package com.back.domain.note.dto;

import java.util.List;

public record NotePageDto(
        List<NoteDto> notes,
        BookDto bookInfo
) {
}
