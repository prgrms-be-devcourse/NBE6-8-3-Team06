package com.back.domain.review.reviewReport

import com.back.domain.member.member.repository.MemberRepository
import com.back.domain.member.member.service.MemberService
import com.back.domain.review.review.service.ReviewService
import com.back.domain.review.reviewReport.controller.AdmReviewReportController
import com.back.domain.review.reviewReport.controller.ReviewReportController
import com.back.domain.review.reviewReport.service.ReviewReportService
import jakarta.servlet.http.Cookie
import jakarta.transaction.Transactional
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
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
class AdmReviewReportControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val memberRepository: MemberRepository,
    @Autowired private val memberService: MemberService,
    @Autowired private val reviewService: ReviewService,
    @Autowired private val reviewReportService: ReviewReportService,
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
    @DisplayName("검색")
    fun t1(){
        val review = reviewService.findLatest()?:throw RuntimeException("cound not find latest review")
        val accessTokens = makeAccessTokens(1)
        val adminAccessToken = adminAccessToken()
        createReviewReport(review.id, "test", accessTokens[0])
        val reviewReport = reviewReportService.getLatest()?:throw RuntimeException("cound not find latest review report")
        mockMvc.perform(
            MockMvcRequestBuilders.get("/adm/reviews/report")
                .cookie(Cookie("accessToken", adminAccessToken))
        ).andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.handler().handlerType(AdmReviewReportController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("search"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("review successfully searched"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].id").value(reviewReport.id))
    }

    @Test
    fun t2(){
        val review = reviewService.findLatest()?:throw RuntimeException("cound not find latest review")
        val accessTokens = makeAccessTokens(1)
        val adminAccessToken = adminAccessToken()
        createReviewReport(review.id, "test", accessTokens[0])
        val reviewReport = reviewReportService.getLatest()?:throw RuntimeException("cound not find latest review report")
        mockMvc.perform(
            MockMvcRequestBuilders.put("/adm/reviews/report/{report_id}", reviewReport.id)
                .contentType("application/json")
                .content("""
                    {
                      "process": "ACCEPT",
                      "answer": "test"
                    }
                """.trimIndent()
                )
                .cookie(Cookie("accessToken", adminAccessToken))
        ).andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.handler().handlerType(AdmReviewReportController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("process"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("review successfully proceed"))
    }
}