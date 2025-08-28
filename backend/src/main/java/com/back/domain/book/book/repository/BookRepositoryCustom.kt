package com.back.domain.book.book.repository

import com.back.domain.book.book.entity.Book
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BookRepositoryCustom {

    fun findByTitleOrAuthorContaining(keyword: String): List<Book>

    fun findValidBooksByTitleOrAuthorContaining(query: String): List<Book>

    fun findValidBooksByTitleOrAuthorContainingWithPaging(query: String, pageable: Pageable): Page<Book>

    fun findAllValidBooks(pageable: Pageable): Page<Book>

    fun findValidBookByIsbn13(isbn13: String): Book?

    fun findValidBooksByCategory(categoryName: String, pageable: Pageable): Page<Book>

    fun findValidBooksByQueryAndCategory(query: String, categoryName: String, pageable: Pageable): Page<Book>
}