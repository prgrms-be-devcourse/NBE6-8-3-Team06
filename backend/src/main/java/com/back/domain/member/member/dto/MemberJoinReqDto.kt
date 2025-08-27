package com.back.domain.member.member.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@JvmRecord
data class MemberJoinReqDto(
    @JvmField val email: @NotBlank @Size(min = 2, max = 30) String?,
    @JvmField val name: @NotBlank @Size(min = 2, max = 20) String?,
    @JvmField val password: @NotBlank @Size(min = 2, max = 20) String?
) 