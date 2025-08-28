package com.back.domain.member.member.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class MemberJoinReqDto(
    @field:NotBlank
    @field:Size(min = 2, max = 30)
    val email: String,
    
    @field:NotBlank
    @field:Size(min = 2, max = 20)
    val name: String,
    
    @field:NotBlank
    @field:Size(min = 2, max = 20)
    val password: String
)
