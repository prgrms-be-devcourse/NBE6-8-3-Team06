package com.back.domain.note.dto

data class BookDto(
    val imageUrl: String?,
    val title: String,
    val author: List<String>,
    val category: String
)
