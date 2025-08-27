package com.back.domain.member.member.dto;

public record MemberLoginResDto(
    MemberDto memDto,
    String accessToken
) {}