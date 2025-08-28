package com.back.domain.book.author.repository

import com.back.domain.book.author.entity.Author
import org.springframework.data.jpa.repository.JpaRepository

interface AuthorRepository : JpaRepository<Author, Int> {
    fun findByName(name: String?): Author?
}