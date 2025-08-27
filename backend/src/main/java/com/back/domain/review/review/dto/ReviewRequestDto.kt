package com.back.domain.review.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewRequestDto(
        @NotBlank String content,
        @NotNull Integer rate
) {
};
