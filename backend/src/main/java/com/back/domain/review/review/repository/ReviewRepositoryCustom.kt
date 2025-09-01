package com.back.domain.review.review.repository

import com.back.domain.book.book.entity.Book
import com.back.domain.member.member.entity.Member
import com.back.domain.review.review.entity.Review
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ReviewRepositoryCustom {

    fun findFirstByOrderByIdDesc(): Review?

    fun findByBookAndMember(book: Book, member: Member): Review?

    fun findAverageRatingByMember(member: Member): Double

    fun findAllByMember(member: Member): MutableList<Review>

    fun findByBookOrderByCreateDateDesc(book: Book, pageable: Pageable): Page<Review>

    fun hardDeleteByElapsedDays(days: Int)

}