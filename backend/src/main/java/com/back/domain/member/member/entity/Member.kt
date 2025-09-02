package com.back.domain.member.member.entity

import com.back.domain.bookmarks.entity.Bookmark
import com.back.domain.member.member.constant.MemberRole
import com.back.domain.note.entity.Note
import com.back.domain.review.review.entity.Review
import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.*

@Entity
class Member(
    private var name: String,
    @Column(unique = true)
    private var email: String,
    private var password: String,
    var profileImgUrl: String? = null,
    @Enumerated(EnumType.STRING)
    private var role: MemberRole = MemberRole.USER
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
        null,
        MemberRole.USER
    ) {
        this.id = id
    }
    // Admin 계정 생성용 생성자 추가
    constructor(name: String, email: String, password: String, role: MemberRole) : this(
        name,
        email,
        password,
        null,
        role
    )
    // 일반 회원 가입용 생성자
    constructor(name: String, email: String, password: String) : this(
        name,
        email,
        password,
        null,
        MemberRole.USER
    )

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
    
    fun getRole(): MemberRole {
        return role
    }
    
    fun setRole(role: MemberRole) {
        this.role = role
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
