package com.back.domain.member.member.dto

@JvmRecord
data class MemberLoginResDto(
    val memDto: MemberDto?,
    val accessToken: String?
) 