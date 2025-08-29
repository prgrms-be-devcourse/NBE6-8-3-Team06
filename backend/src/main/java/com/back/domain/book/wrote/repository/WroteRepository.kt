package com.back.domain.book.wrote.repository

import com.back.domain.book.author.entity.Author
import com.back.domain.book.book.entity.Book
import com.back.domain.book.wrote.entity.Wrote
import org.springframework.data.jpa.repository.JpaRepository

interface WroteRepository : JpaRepository<Wrote, Int> {
    fun existsByAuthorAndBook(author: Author, book: Book): Boolean
}