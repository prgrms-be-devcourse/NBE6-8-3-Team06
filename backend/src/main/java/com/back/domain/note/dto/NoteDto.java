package com.back.domain.note.dto;

import com.back.domain.note.entity.Note;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;

public record NoteDto(
        @NonNull int id,
        @NonNull LocalDateTime createDate,
        @NonNull LocalDateTime modifyDate,
        @NonNull String title,
        @NonNull String content,
        String page
) {
    public NoteDto(Note note) {
        this(
                note.getId(),
                note.getCreateDate(),
                note.getModifyDate(),
                note.getTitle(),
                note.getContent(),
                note.getPage()
        );
    }
}
