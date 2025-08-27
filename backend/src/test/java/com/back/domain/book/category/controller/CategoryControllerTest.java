package com.back.domain.book.category.controller;

import com.back.domain.book.category.dto.CategoryDto;
import com.back.domain.book.category.entity.Category;
import com.back.domain.book.category.service.CategoryService;
import com.back.domain.bookmarks.controller.BookmarkController;
import com.back.domain.bookmarks.dto.BookmarkReadStatesDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class CategoryControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private CategoryService categoryService;

    @Test
    @DisplayName("카테고리 목록 조회")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/categories")
                )
                .andDo(print());
        List<CategoryDto> categories = categoryService.getCategories();

        resultActions
                .andExpect(handler().handlerType(CategoryController.class))
                .andExpect(handler().methodName("getCategories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("조회 성공"));

        for(int i=0; i< categories.size(); i++){
            CategoryDto category = categories.get(i);
            resultActions
                    .andExpect(jsonPath("$.data[%d].name".formatted(i)).value(category.name()));
        }
    }
}
