package com.back.domain.bookmarks.repository

import com.back.domain.book.book.entity.Book
import com.back.domain.bookmarks.entity.Bookmark
import com.back.domain.member.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface BookmarkRepository : JpaRepository<Bookmark, Int>, JpaSpecificationExecutor<Bookmark?>, BookmarkRepositoryCustom {
    fun findByBook(book: Book): Optional<Bookmark>
    fun findByIdAndMember(id: Int, member: Member): Optional<Bookmark>
    fun findByMember(member: Member): MutableList<Bookmark>
    fun findByMemberAndBook(member: Member, book: Book): Optional<Bookmark>

    @Query("SELECT b FROM Bookmark b WHERE b.member = :member AND b.book.id IN :bookIds")
    fun findByMemberAndBookIds(
        @Param("member") member: Member,
        @Param("bookIds") bookIds: MutableList<Int?>
    ): MutableList<Bookmark>

    @Query("SELECT b FROM Bookmark b WHERE b.member = :member AND b.book = :book")
    fun findByMemberAndBookWithFresh(@Param("member") member: Member, @Param("book") book: Book): Optional<Bookmark>
    fun existsByMemberAndBook(member: Member, book: Book): Boolean
    fun getBookmarkByMemberOrderByIdDesc(member: Member): Optional<Bookmark>
}
