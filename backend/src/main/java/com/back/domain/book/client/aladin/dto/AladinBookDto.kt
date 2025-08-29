package com.back.domain.book.client.aladin.dto

import java.time.LocalDateTime

data class AladinBookDto(
    val title: String? = null,
    val imageUrl: String? = null,
    val publisher: String? = null,
    val isbn13: String? = null,
    val totalPage: Int = 0,
    val publishedDate: LocalDateTime? = null,
    val categoryName: String? = null,
    val mallType: String? = null,
    val authors: List<String> = emptyList()
)