package com.back.domain.member.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberJoinReqDto(
    @NotBlank
    @Size(min = 2, max = 30)
    String email,
    @NotBlank
    @Size(min = 2, max = 20)
    String name,
    @NotBlank
    @Size(min = 2, max = 20)
    String password
) {}