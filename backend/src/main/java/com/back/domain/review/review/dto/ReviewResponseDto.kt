package com.back.domain.review.review.dto

import java.time.LocalDateTime


data class ReviewResponseDto(
    val id: Int,
    val content: String,
    val rate: Int,
    val memberName: String,
    val memberId: Int,
    val likeCount: Int,
    val dislikeCount: Int,
    val spoiler: Boolean,
    val isRecommended: Boolean?,
    val createdDate: LocalDateTime,
    val modifiedDate: LocalDateTime
) {
}
