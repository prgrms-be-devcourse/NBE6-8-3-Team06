package com.back.domain.review.review.dto

data class ReviewDetailResponseDto(
    val id:Int,
    val content: String,
    val rate: Int,
    val memberName: String,

)
