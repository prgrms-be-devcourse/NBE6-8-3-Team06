package com.back.domain.bookmarks.dto

import com.back.domain.book.book.entity.Book
import com.back.domain.book.wrote.entity.Wrote
import java.time.LocalDateTime


data class BookmarkBookDetailDto(
    val id: Int,
    val isbn13: String,
    val title: String,
    val imageUrl: String,
    val publisher: String,
    val totalPage: Int,
    val avgRate: Double,
    val category: String,
    val authors: MutableList<String>,
    val publishDate: LocalDateTime
) {
    constructor(book: Book) : this(
        book.id,
        book.getIsbn13(),
        book.getTitle(),
        book.getImageUrl(),
        book.getPublisher(),
        book.getTotalPage(),
        book.getAvgRate().toDouble(),
        book.getCategory().getName(),
        if (book.getAuthors() != null) book.getAuthors().stream()
            .map<String?> { a: Wrote -> a.getAuthor().getName() }.toList() else mutableListOf<String>(),
        book.getPublishedDate()
    )
}
