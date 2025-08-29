package com.back.domain.book.category.service

import com.back.domain.book.category.dto.CategoryDto
import com.back.domain.book.category.repository.CategoryRepository
import org.springframework.stereotype.Service

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository
) {

    fun getCategories(): List<CategoryDto> {
        return categoryRepository.findAll()
            .map { CategoryDto.from(it) }
    }
}