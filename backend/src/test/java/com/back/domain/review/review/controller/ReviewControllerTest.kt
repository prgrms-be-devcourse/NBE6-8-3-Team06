package com.back.domain.review.review.controller

import com.back.domain.book.book.repository.BookRepository
import com.back.domain.member.member.service.AuthTokenService
import com.back.domain.member.member.service.MemberService
import com.back.domain.review.review.entity.Review
import com.back.domain.review.review.service.ReviewService
import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.util.function.Supplier

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class ReviewControllerTest(
    @Autowired
    private val mvc: MockMvc,
    @Autowired
    private val reviewService: ReviewService,
    @Autowired
    private val memberService: MemberService,
    @Autowired
    private val authTokenService: AuthTokenService,
    @Autowired
    private val bookRepository: BookRepository
) {
    @Throws(Exception::class)
    fun addReview(bookId: Int, accessToken: String): ResultActions {
        return mvc.perform(
            MockMvcRequestBuilders.post("/reviews/{book_id}", bookId)
                .contentType("application/json")
                .content(
                    """
{
    "content": "이 책 정말 좋았어요!",
    "rate": 5
}

""".trimIndent()
                ).cookie(Cookie("accessToken", accessToken))
        ).andDo(MockMvcResultHandlers.print())
    }

    @Test
    @DisplayName("리뷰 작성")
    @Throws(Exception::class)
    fun t1() {
        val member = memberService.findByEmail("email1@a.a")?:throw IllegalArgumentException("해당 이메일의 멤버가 없음")
        val accessToken = authTokenService.genAccessToken(member)?: throw RuntimeException("해당 멤버에게 accessToken 발급 실패")
        val book = bookRepository.findAll().get(1)
        val resultActions = addReview(book.id, accessToken)
        val review =
            reviewService.findLatest().orElseThrow<RuntimeException?>(Supplier { RuntimeException("리뷰가 없습니다.") })
        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("create"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("201-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Reviews fetched successfully"))


        assertThat(review.id).isGreaterThan(0)
        assertThat(review.content).isEqualTo("이 책 정말 좋았어요!")
        assertThat(review.rate).isEqualTo(5)
    }

    @Test
    @DisplayName("리뷰 작성 여러번 - 실패")
    @Throws(Exception::class)
    fun t1_2() {
        val member = memberService.findByEmail("email1@a.a")?:throw IllegalArgumentException("해당 이메일의 멤버가 없음")
        val accessToken = authTokenService.genAccessToken(member)?: throw RuntimeException("해당 멤버에게 accessToken 발급 실패")
        val book = bookRepository.findAll().get(1)
        val count = reviewService.count()
        addReview(book.id, accessToken)
        val resultActions = addReview(book.id, accessToken)
        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("create"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("400-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Review already exists"))
        assertThat(reviewService.count() - count).isEqualTo(1)
    }

    @Test
    @DisplayName("리뷰 삭제")
    @Throws(Exception::class)
    fun t2() {
        val member = memberService.findByEmail("email1@a.a")?:throw IllegalArgumentException("해당 이메일의 멤버가 없음")
        val accessToken = authTokenService.genAccessToken(member)?: throw RuntimeException("해당 멤버에게 accessToken 발급 실패")
        val book = bookRepository.findAll().get(1)
        addReview(book.id, accessToken)
        val review =
            reviewService.findLatest().orElseThrow(Supplier { RuntimeException("리뷰가 없습니다.") })
        AssertionsForClassTypes.assertThat(review.id).isGreaterThan(0)

        val resultActions = mvc.perform(
            MockMvcRequestBuilders.delete("/reviews/{book_id}", book.id)
                .contentType("application/json")
                .cookie(Cookie("accessToken", accessToken))
        ).andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("delete"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Review deleted successfully"))

        assertThat<Review>(reviewService.findById(review.id)).isEmpty()
    }

    @Test
    @DisplayName("리뷰 수정")
    @Throws(Exception::class)
    fun t3() {
        val member = memberService.findByEmail("email1@a.a")?:throw IllegalArgumentException("해당 이메일의 멤버가 없음")
        val accessToken = authTokenService.genAccessToken(member)?: throw RuntimeException("해당 멤버에게 accessToken 발급 실패")
        val book = bookRepository.findAll().get(1)
        addReview(book.id, accessToken)
        var review =
            reviewService.findLatest().orElseThrow(Supplier { RuntimeException("리뷰가 없습니다.") })
        AssertionsForClassTypes.assertThat(review.id).isGreaterThan(0)
        val resultActions = mvc.perform(
            MockMvcRequestBuilders.put("/reviews/{book_id}", book.id)
                .contentType("application/json")
                .content(
                    """
{
    "content": "다시 읽다보니 그렇게 좋지는 않네요.",
    "rate": 4
}

""".trimIndent()
                ).cookie(Cookie("accessToken", accessToken))
        ).andDo(MockMvcResultHandlers.print())
        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("modify"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Review modified successfully"))

        review = reviewService.findLatest().orElseThrow(Supplier { RuntimeException("리뷰가 없습니다.") })
        assertThat(review.id).isGreaterThan(0)
        assertThat(review.content).isEqualTo("다시 읽다보니 그렇게 좋지는 않네요.")
        assertThat(review.rate).isEqualTo(4)
    }

    @Test
    @DisplayName("리뷰 조회")
    @Throws(Exception::class)
    fun t4() {
        val member = memberService.findByEmail("email1@a.a")?:throw IllegalArgumentException("해당 이메일의 멤버가 없음")
        val accessToken = authTokenService.genAccessToken(member)?: throw RuntimeException("해당 멤버에게 accessToken 발급 실패")
        val book = bookRepository.findAll().get(1)
        addReview(book.id, accessToken)
        val review =
            reviewService.findLatest().orElseThrow(Supplier { RuntimeException("리뷰가 없습니다.") })
        AssertionsForClassTypes.assertThat(review.id).isGreaterThan(0)

        val resultActions = mvc.perform(
            MockMvcRequestBuilders.get("/reviews/{book_id}", book.id)
                .cookie(Cookie("accessToken", accessToken))
        ).andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getReview"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Review read successfully"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value(review.content))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.rate").value(review.rate))
    }

    @Test
    @DisplayName("리뷰 조회 - 실패 (리뷰 없음)")
    @Throws(Exception::class)
    fun t4_2() {
        val member = memberService.findByEmail("email1@a.a")?:throw IllegalArgumentException("해당 이메일의 멤버가 없음")
        val accessToken = authTokenService.genAccessToken(member)?: throw RuntimeException("해당 멤버에게 accessToken 발급 실패")
        val book = bookRepository.findAll().get(1)

        val resultActions = mvc.perform(
            MockMvcRequestBuilders.get("/reviews/{book_id}", book.id)
                .cookie(Cookie("accessToken", accessToken))
        ).andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getReview"))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("404-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Review not found"))
    }
}
