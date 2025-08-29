package com.back.domain.book.book.repository

import com.back.domain.book.book.entity.Book
import org.springframework.data.jpa.repository.JpaRepository

interface BookRepository : JpaRepository<Book, Int>, BookRepositoryCustom {

    // 간단한 조회는 Spring Data JPA 기본 메서드 활용
    fun findByIsbn13(isbn13: String): Book?
}