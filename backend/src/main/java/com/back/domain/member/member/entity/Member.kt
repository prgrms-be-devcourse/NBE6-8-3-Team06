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
    private var password: String,
    var profileImgUrl: String? = null
) : BaseEntity() {
    @Column(length = 1000)
    private var refreshToken: String? = null

    @OneToMany(mappedBy = "member", cascade = [CascadeType.ALL], orphanRemoval = true)
    var reviews: MutableList<Review> = mutableListOf()

    @OneToMany(mappedBy = "member", cascade = [CascadeType.ALL], orphanRemoval = true)
    var bookmarks: MutableList<Bookmark> = mutableListOf()

    @OneToMany(mappedBy = "member", cascade = [CascadeType.ALL], orphanRemoval = true)
    var notes: MutableList<Note> = mutableListOf()

    // JPA와 상관없는 객체를 만들 때 사용, SecurityUser로부터 정보를 받아와서 생성할 때 사용
    constructor(id: Int, email: String, name: String) : this(
        name,
        email,
        "",
        null
    ) {
        this.id = id
    }

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
    
    val profileImgUrlOrDefault: String
        get() {
            profileImgUrl?.let { return it }
            return "https://placehold.co/600x600?text=U_U"
        }

    // 문서 패턴에서 본 modify 메서드만 추가
    fun modify(name: String, profileImgUrl: String?) {
        this.name = name
        this.profileImgUrl = profileImgUrl
    }
}
