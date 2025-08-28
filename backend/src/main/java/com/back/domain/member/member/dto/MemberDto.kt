package com.back.domain.member.member.dto

import com.back.domain.member.member.entity.Member

data class MemberDto(
    val email: String?,
    val name: String?,
    val password: String?
) {
    constructor(member: Member) : this(
        email = member.getEmail(),
        name = member.getName(),
        password = member.getPassword()
    )
}
