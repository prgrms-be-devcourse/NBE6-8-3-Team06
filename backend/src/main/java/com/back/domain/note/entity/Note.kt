package com.back.domain.note.entity

import com.back.domain.bookmarks.entity.Bookmark
import com.back.domain.member.member.entity.Member
import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.*
import jakarta.persistence.FetchType.LAZY

@Entity
open class Note (
    @field:ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    var member: Member,

    @Column(nullable = false, length = 250)
    var title: String,

    @Column(nullable = false, length = 5000)
    var content: String,

    @Column(nullable = true)
    var page: String? = null,

    @field:ManyToOne(fetch = LAZY)
    @JoinColumn(name = "bookmark_id")
    var bookmark: Bookmark

) : BaseEntity() {
    fun modify(title: String, content: String, page: String?) {
        this.title = title
        this.content = content
        this.page = page
    }

}