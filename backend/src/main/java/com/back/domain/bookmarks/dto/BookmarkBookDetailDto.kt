package com.back.domain.bookmarks.dto

import com.back.domain.book.book.entity.Book
import com.back.domain.book.wrote.entity.Wrote
import java.time.LocalDateTime


data class BookmarkBookDetailDto(
    val id: Int,
    val isbn13: String?,
    val title: String?,
    val imageUrl: String?,
    val publisher: String?,
    val totalPage: Int,
    val avgRate: Double,
    val category: String?,
    val authors: List<String>,
    val publishDate: LocalDateTime?
) {
    constructor(book: Book) : this(
        book.id,
        book.isbn13,
        book.title,
        book.imageUrl,
        book.publisher,
        book.totalPage,
        book.avgRate.toDouble(),
        book.category.name,
        book.authors.map { a: Wrote -> a.author.name },
        book.publishedDate
    )
}
