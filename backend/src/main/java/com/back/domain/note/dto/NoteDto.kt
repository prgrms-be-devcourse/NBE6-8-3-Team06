package com.back.domain.note.dto

import com.back.domain.note.entity.Note
import org.springframework.lang.NonNull
import java.time.LocalDateTime

@JvmRecord
data class NoteDto(
    @field:NonNull @param:NonNull val id: Int,
    @field:NonNull @param:NonNull val createDate: LocalDateTime,
    @field:NonNull @param:NonNull val modifyDate: LocalDateTime,
    @field:NonNull @param:NonNull val title: String,
    @field:NonNull @param:NonNull val content: String,
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