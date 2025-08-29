package com.back.domain.book.category.repository

import com.back.domain.book.category.entity.Category
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, Int> {
    fun findByName(name: String?): Category?
}
