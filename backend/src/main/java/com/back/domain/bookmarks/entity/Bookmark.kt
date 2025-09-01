package com.back.domain.bookmarks.entity

import com.back.domain.book.book.entity.Book
import com.back.domain.bookmarks.constant.ReadState
import com.back.domain.member.member.entity.Member
import com.back.domain.note.entity.Note
import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Entity
class Bookmark(
    @field:JoinColumn(name = "book_id")
    @field:ManyToOne(fetch = FetchType.LAZY)
    var book: Book,
    @field:JoinColumn(name = "member_id")
    @field:ManyToOne(fetch = FetchType.LAZY)
    var member: Member
) : BaseEntity() {
    @Enumerated(EnumType.STRING)
    lateinit var readState: ReadState

    var readPage: Int = 0

    var startReadDate: LocalDateTime? = null

    var endReadDate: LocalDateTime? = null


    fun updateReadState(readState: ReadState) {
        if (readState == ReadState.WISH) {
            this.startReadDate = null
            this.endReadDate = null
            this.readPage = 0
        }
        if (readState == ReadState.READING) {
            this.endReadDate = null
        }
        this.readState = readState
    }

    fun updateReadPage(readPage: Int) {
        this.readPage = readPage
    }

    fun updateStartReadDate(startReadDate: LocalDateTime) {
        this.startReadDate = startReadDate
    }

    fun updateEndReadDate(endReadDate: LocalDateTime) {
        this.endReadDate = endReadDate
    }

    @OneToMany(mappedBy = "bookmark", cascade = [CascadeType.PERSIST, CascadeType.REMOVE], orphanRemoval = true)
    val notes: MutableList<Note> = ArrayList()

    init {
        this.readState = ReadState.WISH
    }

    fun calculateReadingRate(): Int {
        if (readState == ReadState.WISH) return 0
        val totalPage = book.totalPage
        if (totalPage == 0) return 0
        if (readPage >= totalPage) return 100
        if (readPage <= 0) return 0
        val rate = (readPage.toDouble() / totalPage.toDouble()) * 100
        return Math.round(rate).toInt()
    }

    fun calculateReadingDuration(): Long {
        if (readState == ReadState.WISH) return 0
        val effectiveEnd = (if (endReadDate == null) java.time.LocalDateTime.now() else endReadDate)
        return ChronoUnit.DAYS.between(startReadDate, effectiveEnd) + 1
    }
}
