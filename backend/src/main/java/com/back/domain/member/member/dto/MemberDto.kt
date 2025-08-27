package com.back.domain.member.member.dto

import com.back.domain.member.member.entity.Member

@JvmRecord
data class MemberDto(val email: String?, val name: String?, val password: String?) {
    constructor(member: Member) : this(
        member.getEmail(),
        member.getName(),
        member.getPassword()
    )
}
