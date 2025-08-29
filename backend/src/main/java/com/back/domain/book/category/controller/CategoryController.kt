package com.back.domain.book.category.controller

import com.back.domain.book.category.dto.CategoryDto
import com.back.domain.book.category.service.CategoryService
import com.back.global.rsData.RsData
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/categories")
class CategoryController(
    private val categoryService: CategoryService
) {

    @Transactional(readOnly = true)
    @GetMapping
    fun getCategories(): RsData<List<CategoryDto>> {
        return RsData("200-1", "조회 성공", categoryService.getCategories())
    }
}
