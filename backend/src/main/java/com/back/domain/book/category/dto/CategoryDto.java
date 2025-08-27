package com.back.domain.book.category.dto;

import com.back.domain.book.category.entity.Category;

public record CategoryDto(
        String name
) {
    public CategoryDto(Category category) {
        this(category.getName());
    }
}
