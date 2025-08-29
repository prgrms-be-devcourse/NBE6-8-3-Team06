package com.back.domain.book.book.entity

import com.back.domain.book.category.entity.Category
import com.back.domain.book.wrote.entity.Wrote
import com.back.domain.bookmarks.entity.Bookmark
import com.back.domain.review.review.entity.Review
import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Book @JvmOverloads constructor(
    @Column(nullable = false)
    var title: String = "",

    var publisher: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    var category: Category? = null
) : BaseEntity() {

    var imageUrl: String? = null
    var totalPage: Int = 0
    var publishedDate: LocalDateTime? = null
    var avgRate: Float = 0.0f

    @Column(unique = true)
    var isbn13: String? = null

    @OneToMany(mappedBy = "book", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val _authors: MutableList<Wrote> = mutableListOf()

    @OneToMany(mappedBy = "book", orphanRemoval = true)
    private val _reviews: MutableList<Review> = mutableListOf()

    @OneToMany(mappedBy = "book", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val _bookmarks: MutableList<Bookmark> = mutableListOf()

    // Read-only access to collections
    val authors: List<Wrote>
        get() = _authors.toList()

    val reviews: List<Review>
        get() = _reviews.toList()

    val bookmarks: List<Bookmark>
        get() = _bookmarks.toList()

    // Collection management methods
    fun addAuthor(wrote: Wrote) {
        _authors.add(wrote)
    }

    fun removeAuthor(wrote: Wrote) {
        _authors.remove(wrote)
    }

    fun addReview(review: Review) {
        _reviews.add(review)
    }

    fun removeReview(review: Review) {
        _reviews.remove(review)
    }

    fun addBookmark(bookmark: Bookmark) {
        _bookmarks.add(bookmark)
    }

    fun removeBookmark(bookmark: Bookmark) {
        _bookmarks.remove(bookmark)
    }
}