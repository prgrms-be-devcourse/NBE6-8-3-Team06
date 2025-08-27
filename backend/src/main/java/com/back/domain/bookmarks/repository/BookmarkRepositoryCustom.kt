package com.back.domain.bookmarks.repository

import com.back.domain.bookmarks.dto.ReadStateCount
import com.back.domain.bookmarks.entity.Bookmark
import com.back.domain.member.member.entity.Member
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BookmarkRepositoryCustom {
    fun search(
        member: Member,
        category: String?,
        state: String?,
        keyword: String?,
        pageable: Pageable
    ): Page<Bookmark>

    fun countReadState(member: Member, category: String?, state: String?, keyword: String?): ReadStateCount
}
