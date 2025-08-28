package com.back.domain.note.dto

import org.springframework.lang.NonNull

@JvmRecord
data class BookDto(
    @field:NonNull @param:NonNull val imageUrl: String,
    @field:NonNull @param:NonNull val title: String,
    @field:NonNull @param:NonNull val author: MutableList<String?>,
    @field:NonNull @param:NonNull val category: String
)
