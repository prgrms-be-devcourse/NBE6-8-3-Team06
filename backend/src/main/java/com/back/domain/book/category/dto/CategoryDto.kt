package com.back.domain.book.category.dto

import com.back.domain.book.category.entity.Category

data class CategoryDto(
    val name: String?
) {
    companion object {
        @JvmStatic
        fun from(category: Category) = CategoryDto(category.name)
    }
}