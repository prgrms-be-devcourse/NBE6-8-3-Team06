package com.back.domain.bookmarks.dto;

import com.back.domain.review.review.entity.Review;

import java.time.LocalDateTime;

public record BookmarkReviewDetailDto(
        int id,
        String content,
        double rate,
        LocalDateTime date
) {
    public BookmarkReviewDetailDto(Review review){
        this(
                review.getId(),
                review.getContent(),
                review.getRate(),
                review.getModifyDate()
        );
    }
}
