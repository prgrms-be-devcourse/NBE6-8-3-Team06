package com.back.global.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponseDto<T>(
        List<T> data,
        int pageNumber,
        int pageSize,
        int totalPages,
        long totalElements,
        boolean isLast
) {
    public PageResponseDto(Page<T> page) {
        this(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.isLast()
        );
    }
}
