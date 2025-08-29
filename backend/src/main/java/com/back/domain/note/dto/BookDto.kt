package com.back.domain.note.dto

import org.springframework.lang.NonNull

data class BookDto(
    val imageUrl: String?,
    val title: String,
    val author: List<String>,
    val category: String
)
