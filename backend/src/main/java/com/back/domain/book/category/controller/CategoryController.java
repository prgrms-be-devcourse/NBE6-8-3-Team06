package com.back.domain.book.category.controller;

import com.back.domain.book.category.dto.CategoryDto;
import com.back.domain.book.category.service.CategoryService;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    @Transactional(readOnly = true)
    public RsData<List<CategoryDto>> getCategories() {
        return new RsData<>("200-1", "조회 성공", categoryService.getCategories());
    }

}
