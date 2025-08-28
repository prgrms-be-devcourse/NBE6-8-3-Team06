package com.back.domain.member.member.dto

data class MemberLoginResDto(
    val memberDto: MemberDto?,
    val accessToken: String?
)
