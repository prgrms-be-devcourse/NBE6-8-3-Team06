package com.back.domain.review.review.repository

import com.back.domain.book.book.entity.Book
import com.back.domain.member.member.entity.Member
import com.back.domain.review.review.entity.Review
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface ReviewRepository : JpaRepository<Review, Int>, ReviewRepositoryCustom {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findWithLockById(id: Int): Review?

    fun findByBook(book: Book, pageable: Pageable): Page<Review>
}
