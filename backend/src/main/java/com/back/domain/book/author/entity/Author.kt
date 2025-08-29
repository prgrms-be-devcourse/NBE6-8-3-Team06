package com.back.domain.book.author.entity

import com.back.domain.book.wrote.entity.Wrote
import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity
class Author(var name: String) : BaseEntity() {
    @OneToMany(mappedBy = "author", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val _books: MutableList<Wrote> = mutableListOf()

    val books: List<Wrote>
        get() = _books

    fun addBook(wrote: Wrote) {
        _books.add(wrote)
    }

    fun removeBook(wrote: Wrote) {
        _books.remove(wrote)
    }
}
