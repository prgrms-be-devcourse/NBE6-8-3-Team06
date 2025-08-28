package com.back.domain.note.dto

@JvmRecord
data class NotePageDto(
    val notes: MutableList<NoteDto?>?,
    val bookInfo: BookDto?
)
