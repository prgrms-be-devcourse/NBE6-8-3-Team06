package com.back.global.dto

import org.springframework.data.domain.Page

data class PageResponseDto<T>(
    val data: MutableList<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalElements: Long,
    val isLast: Boolean
) {
    constructor(page: Page<T>) : this(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalPages(),
        page.getTotalElements(),
        page.isLast()
    )
}
