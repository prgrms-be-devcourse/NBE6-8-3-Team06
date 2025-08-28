package com.back.domain.note.dto

import com.back.domain.note.entity.Note
import org.springframework.lang.NonNull
import java.time.LocalDateTime

data class NoteDto(
    val id: Int,
    val createDate: LocalDateTime,
    val modifyDate: LocalDateTime,
    val title: String,
    val content: String,
    val page: String?
) {
    constructor(note: Note) : this(
        note.id,
        note.createDate,
        note.modifyDate,
        note.title,
        note.content,
        note.page
    )
}