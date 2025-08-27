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

interface ReviewRepository : JpaRepository<Review, Int> {
    fun findFirstByOrderByIdDesc(): Optional<Review>


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findWithLockById(id: Int): Optional<Review>

    fun findByBookAndMember(book: Book, member: Member): Optional<Review>

    @Query("SELECT AVG(rate) FROM Review WHERE member= :member")
    fun findAverageRatingByMember(@Param("member") member: Member): Optional<Double>

    fun findAllByMember(member: Member): MutableList<Review>

    fun findByBookOrderByCreateDateDesc(book: Book, pageable: Pageable): Page<Review>

    fun findByBook(book: Book, pageable: Pageable): Page<Review>
}
