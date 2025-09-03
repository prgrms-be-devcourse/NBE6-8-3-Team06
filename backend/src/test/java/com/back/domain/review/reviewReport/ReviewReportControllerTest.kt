package com.back.domain.review.reviewReport


import com.back.domain.member.member.repository.MemberRepository
import com.back.domain.member.member.service.MemberService
import com.back.domain.review.review.service.ReviewService
import com.back.domain.review.reviewReport.controller.ReviewReportController
import jakarta.servlet.http.Cookie
import jakarta.transaction.Transactional
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@Rollback
class ReviewReportControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val reviewService: ReviewService,
    @Autowired private val memberService: MemberService,
    @Autowired private val memberRepository: MemberRepository,
) {

    fun makeAccessTokens(count: Int): List<String>{
        return memberRepository.findAll().take(count).map { member -> memberService.geneAccessToken(member) }
    }

    fun adminAccessToken():String{
        val admin = memberService.findByEmail("admin@a.a")?:throw RuntimeException("admin not found")
        return memberService.geneAccessToken(admin)
    }

    fun createReviewReport(reviewId: Int, content: String = "test", accessToken: String): ResultActions {
        return mockMvc.perform(
            MockMvcRequestBuilders.post("/reviews/{reviewId}/report", reviewId)
                .contentType("application/json")
                .content("""
                    {
                      "reason":"${content}" 
                    }
                """.trimIndent())
                .cookie(Cookie("accessToken", accessToken))
        ).andDo(MockMvcResultHandlers.print())
    }

    @Test
    @DisplayName("리뷰 신고 생성")
    fun t1(){
        val review = reviewService.findLatest()?:throw RuntimeException("cound not find latest review")
        val accessToken = makeAccessTokens(1)[0]
        createReviewReport(review.id, "test", accessToken)
            .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewReportController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("createReviewReport"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("201-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("success to create review report"))
    }


}