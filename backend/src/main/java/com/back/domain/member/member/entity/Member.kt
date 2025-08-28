package com.back.domain.member.member.entity

import com.back.domain.bookmarks.entity.Bookmark
import com.back.domain.note.entity.Note
import com.back.domain.review.review.entity.Review
import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany


@Entity
class Member(
    private var name: String,
    @Column(unique = true)
    private var email: String,
    private var password: String
) : BaseEntity() {
    @Column(length = 1000)
    private var refreshToken: String? = null

    @OneToMany(mappedBy = "member", cascade = [CascadeType.ALL], orphanRemoval = true)
    var reviews: MutableList<Review> = mutableListOf()

    @OneToMany(mappedBy = "member", cascade = [CascadeType.ALL], orphanRemoval = true)
    var bookmarks: MutableList<Bookmark> = mutableListOf()

    @OneToMany(mappedBy = "member", cascade = [CascadeType.ALL], orphanRemoval = true)
    var notes: MutableList<Note> = mutableListOf()

    fun updateRefreshToken(refreshToken: String?) {
        this.refreshToken = refreshToken
    }

    fun clearRefreshToken() {
        refreshToken = null
    }

    fun getEmail(): String {
        return email
    }
    fun getPassword(): String {
        return password
    }
    fun getName(): String {
        return name
    }
    fun getRefreshToken(): String? {
        return refreshToken
    }
}
