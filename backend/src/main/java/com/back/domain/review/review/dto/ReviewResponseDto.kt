package com.back.domain.review.review.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewResponseDto {
    private int id;
    private String content;
    private int rate;
    private String memberName;
    private int memberId;
    private int likeCount;
    private int dislikeCount;
    private Boolean isRecommended;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}