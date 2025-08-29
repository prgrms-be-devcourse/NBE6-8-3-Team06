package com.back.domain.book.category.entity

import com.back.domain.book.book.entity.Book
import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity
class Category(
    var name: String
) : BaseEntity() {

    @OneToMany(mappedBy = "category")
    private val _books: MutableList<Book> = mutableListOf()

    val books: List<Book>
        get() = _books.toList()

    fun addBook(book: Book) {
        _books.add(book)
    }

    fun removeBook(book: Book) {
        _books.remove(book)
    }
}