package com.back.domain.book.category.service;

import com.back.domain.book.category.dto.CategoryDto;
import com.back.domain.book.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<CategoryDto> getCategories() {
        return categoryRepository.findAll().stream().map(CategoryDto::new).toList();
    }
}
