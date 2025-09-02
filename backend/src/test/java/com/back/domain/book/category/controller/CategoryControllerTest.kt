package com.back.domain.book.category.controller

import com.back.domain.book.category.dto.CategoryDto
import com.back.domain.book.category.service.CategoryService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class CategoryControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var categoryService: CategoryService

    @Test
    @DisplayName("카테고리 목록 조회")
    @Throws(Exception::class)
    fun t1() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/categories")
            )
            .andDo(MockMvcResultHandlers.print())
        val categories = categoryService.getCategories()

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(CategoryController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getCategories"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("조회 성공"))

        for (i in categories.indices) {
            val category = categories[i]
            resultActions
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[$i].name").value(category.name))
        }
    }
}
