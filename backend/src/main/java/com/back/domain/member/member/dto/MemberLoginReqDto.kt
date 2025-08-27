package com.back.domain.member.member.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class MemberLoginReqDto(
    @field:NotBlank
    @field:Size(min = 2, max = 30)
    val email: String,
    
    @field:NotBlank
    @field:Size(min = 2, max = 50)
    val password: String
)
