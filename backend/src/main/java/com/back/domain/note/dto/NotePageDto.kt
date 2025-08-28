package com.back.domain.note.dto

data class NotePageDto(
    val notes: List<NoteDto>,
    val bookInfo: BookDto
)
