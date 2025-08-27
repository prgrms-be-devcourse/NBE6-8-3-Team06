package com.back.domain.review.review.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull


data class ReviewRequestDto(
    @field:NotBlank val content: String,
    val rate: Int
)
