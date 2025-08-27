package com.back.domain.review.reviewRecommend.controller

import com.back.domain.book.book.repository.BookRepository
import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.repository.MemberRepository
import com.back.domain.member.member.service.MemberService
import com.back.domain.review.review.repository.ReviewRepository
import com.back.domain.review.review.service.ReviewService
import com.back.domain.review.reviewRecommend.repository.ReviewRecommendRepository
import jakarta.persistence.EntityManager
import jakarta.servlet.http.Cookie
import jakarta.transaction.Transactional
import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.function.Supplier

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@Rollback
class ReviewRecommendControllerTest(
    @Autowired
    private val mvc: MockMvc,
    @Autowired
    private val memberService: MemberService,
    @Autowired
    private val reviewService: ReviewService,
    @Autowired
    private val memberRepository: MemberRepository,
    @Autowired
    private val bookRepository: BookRepository,
    @Autowired
    private val reviewRecommendRepository: ReviewRecommendRepository,
    @Autowired
    private val reviewRepository: ReviewRepository,
    @Autowired
    private val em: EntityManager
) {


    fun makeAccessTokens(count: Int): MutableList<String> {
        return memberRepository.findAll().stream()
            .limit(count.toLong())
            .map { member: Member -> memberService!!.geneAccessToken(member) }
            .toList()
    }

    @Throws(Exception::class)
    fun createReview(bookId: Int, content: String, rate: Int, accessToken: String) {
        mvc!!.perform(
            MockMvcRequestBuilders.post("/reviews/{book_id}", bookId)
                .contentType("application/json")
                .content(
                    """
    {
        "content": "${content}",
        "rate": "${rate}"
        }

""".trimIndent()
                )
                .cookie(Cookie("accessToken", accessToken))
        ).andDo(MockMvcResultHandlers.print())
    }

    @Throws(Exception::class)
    fun createRecommendReview(reviewId: Int, isRecommend: Boolean, accessToken: String): ResultActions {
        return mvc.perform(
            MockMvcRequestBuilders.post("/reviewRecommend/{review_id}/{isRecommend}", reviewId, isRecommend)
                .cookie(Cookie("accessToken", accessToken))
        ).andDo(MockMvcResultHandlers.print())
    }

    @Throws(Exception::class)
    fun updateRecommendReview(reviewId: Int, isRecommend: Boolean, accessToken: String): ResultActions {
        return mvc.perform(
            MockMvcRequestBuilders.put("/reviewRecommend/{review_id}/{isRecommend}", reviewId, isRecommend)
                .cookie(Cookie("accessToken", accessToken))
        ).andDo(MockMvcResultHandlers.print())
    }

    @Throws(Exception::class)
    fun deleteRecommendReview(reviewId: Int, accessToken: String): ResultActions {
        return mvc.perform(
            MockMvcRequestBuilders.delete("/reviewRecommend/{review_id}", reviewId)
                .cookie(Cookie("accessToken", accessToken))
        ).andDo(MockMvcResultHandlers.print())
    }

    @Test
    @DisplayName("리뷰 추천하기 - 성공")
    @Throws(Exception::class)
    fun t1() {
        val member = memberService.findByEmail("email1@a.a").get()
        val accessToken = memberService.geneAccessToken(member)
        val book = bookRepository.findAll().get(0)
        createReview(book.id, "이 책 정말 좋았어요!", 5, accessToken)
        val review =
            reviewService.findLatest().orElseThrow(Supplier { RuntimeException("리뷰가 없습니다.") })


        val resultActions = mvc.perform(
            MockMvcRequestBuilders.post("/reviewRecommend/{review_id}/{isRecommend}", review.id, true)
                .cookie(Cookie("accessToken", accessToken))
        ).andDo(MockMvcResultHandlers.print())
        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewRecommendController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("recommendReview"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("201-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Review recommended successfully"))
    }

    @Test
    @DisplayName("리뷰 여러명이 추천하기 - 성공")
    @Throws(Exception::class)
    fun t2() {
        val member = memberService.findByEmail("email1@a.a").get()
        val member2 = memberService.findByEmail("email2@a.a").get()
        val member3 = memberService.findByEmail("email3@a.a").get()
        val member4 = memberService.findByEmail("email4@a.a").get()
        val accessToken = memberService.geneAccessToken(member)
        val accessToken2 = memberService.geneAccessToken(member2)
        val accessToken3 = memberService.geneAccessToken(member3)
        val accessToken4 = memberService.geneAccessToken(member4)
        val book = bookRepository.findAll().get(0)
        createReview(book.id, "이 책 정말 좋았어요!", 5, accessToken)


        val review =
            reviewService.findLatest().orElseThrow(Supplier { RuntimeException("리뷰가 없습니다.") })
        mvc!!.perform(
            MockMvcRequestBuilders.post("/reviewRecommend/{review_id}/{isRecommend}", review.id, true)
                .cookie(Cookie("accessToken", accessToken))
        ).andDo(MockMvcResultHandlers.print())

        val resultActions = mvc.perform(
            MockMvcRequestBuilders.post("/reviewRecommend/{review_id}/{isRecommend}", review.id, false)
                .cookie(Cookie("accessToken", accessToken2))
        ).andDo(MockMvcResultHandlers.print())

        mvc.perform(
            MockMvcRequestBuilders.post("/reviewRecommend/{review_id}/{isRecommend}", review.id, true)
                .cookie(Cookie("accessToken", accessToken3))
        ).andDo(MockMvcResultHandlers.print())

        mvc.perform(
            MockMvcRequestBuilders.post("/reviewRecommend/{review_id}/{isRecommend}", review.id, false)
                .cookie(Cookie("accessToken", accessToken4))
        ).andDo(MockMvcResultHandlers.print())
        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewRecommendController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("recommendReview"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("201-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Review recommended successfully"))

        AssertionsForClassTypes.assertThat(review.likeCount).isEqualTo(2)
        AssertionsForClassTypes.assertThat(review.dislikeCount).isEqualTo(2)
    }

    @Test
    @DisplayName("리뷰 추천하기 - 실패 (이미 추천한 리뷰)")
    @Throws(Exception::class)
    fun t3() {
        val member = memberService.findByEmail("email1@a.a").get()
        val accessToken = memberService.geneAccessToken(member)
        val book = bookRepository.findAll().get(0)
        createReview(book.id, "이 책 정말 좋았어요!", 5, accessToken)

        val review =
            reviewService.findLatest().orElseThrow<RuntimeException?>(Supplier { RuntimeException("리뷰가 없습니다.") })
        mvc.perform(
            MockMvcRequestBuilders.post("/reviewRecommend/{review_id}/{isRecommend}", review.id, true)
                .cookie(Cookie("accessToken", accessToken))
        ).andDo(MockMvcResultHandlers.print())
        val resultActions = mvc.perform(
            MockMvcRequestBuilders.post("/reviewRecommend/{review_id}/{isRecommend}", review.id, true)
                .cookie(Cookie("accessToken", accessToken))
        ).andDo(MockMvcResultHandlers.print())
        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewRecommendController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("recommendReview"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("400-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Review recommendation already exists"))
    }

    @Test
    @DisplayName("없는 리뷰 추천하기 - 실패")
    @Throws(Exception::class)
    fun t4() {
        val member = memberService.findByEmail("email1@a.a").get()
        val accessToken = memberService.geneAccessToken(member)
        val book = bookRepository.findAll().get(0)
        createReview(book.id, "이 책 정말 좋았어요!", 5, accessToken)
        val review =
            reviewService.findLatest().orElseThrow<RuntimeException?>(Supplier { RuntimeException("리뷰가 없습니다.") })
        val resultActions = createRecommendReview(-1, true, accessToken)
        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewRecommendController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("recommendReview"))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("404-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Review not found"))
    }

    @Test
    @DisplayName("리뷰 추천 업데이트 - 성공")
    @Throws(Exception::class)
    fun t5() {
        val accessTokens = makeAccessTokens(10)
        val book = bookRepository.findAll().get(0)
        createReview(book.id, "이 책 정말 좋았어요!", 5, accessTokens.get(0))


        val review =
            reviewService.findLatest().orElseThrow(Supplier { RuntimeException("리뷰가 없습니다.") })

        for (i in accessTokens.indices) {
            val isRecommend = i % 2 == 0 // 짝수 인덱스는 추천, 홀수 인덱스는 비추천
            createRecommendReview(review.id, isRecommend, accessTokens.get(i)).andDo(MockMvcResultHandlers.print())
        }

        for (i in accessTokens.indices) {
            val isRecommend = i % 2 != 0 // 짝수 인덱스는 비추천, 홀수 인덱스는 추천
            updateRecommendReview(review.id, isRecommend, accessTokens.get(i)).andDo(MockMvcResultHandlers.print())
        }

        AssertionsForClassTypes.assertThat(review.likeCount).isEqualTo(accessTokens.size / 2)
        AssertionsForClassTypes.assertThat(review.dislikeCount).isEqualTo(accessTokens.size / 2)
    }

    @Test
    @DisplayName("리뷰 추천 업데이트 - 실패")
    @Throws(Exception::class)
    fun t6() {
        val accessTokens = makeAccessTokens(10)
        val book = bookRepository.findAll().get(0)
        createReview(book.id, "이 책 정말 좋았어요!", 5, accessTokens.get(0))
        val review =
            reviewService.findLatest().orElseThrow(Supplier { RuntimeException("리뷰가 없습니다.") })

        for (i in accessTokens.indices) {
            val isRecommend = i % 2 == 0 // 짝수 인덱스는 추천, 홀수 인덱스는 비추천
            createRecommendReview(review.id, isRecommend, accessTokens.get(i)).andDo(MockMvcResultHandlers.print())
        }

        val resultActions = updateRecommendReview(-1, true, accessTokens.get(0))
        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewRecommendController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("modifyRecommendReview"))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("404-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Review not found"))
    }

    @Test
    @DisplayName("리뷰 추천 업데이트 - 실패 (이미 추천한 리뷰)")
    @Throws(Exception::class)
    fun t7() {
        val accessTokens = makeAccessTokens(1)
        val book = bookRepository.findAll().get(0)
        createReview(book.id, "이 책 정말 좋았어요!", 5, accessTokens.get(0))
        val review =
            reviewService.findLatest().orElseThrow(Supplier { RuntimeException("리뷰가 없습니다.") })

        createRecommendReview(review.id, true, accessTokens.get(0)).andDo(MockMvcResultHandlers.print())

        val resultActions = updateRecommendReview(review.id, true, accessTokens.get(0))
        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewRecommendController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("modifyRecommendReview"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("400-2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Review recommendation already set to this value"))
    }

    @Test
    @DisplayName("리뷰 추천 업데이트 - 실패 (추천하지 않은 리뷰)")
    @Throws(Exception::class)
    fun t8() {
        val accessTokens = makeAccessTokens(1)
        val book = bookRepository.findAll().get(0)
        createReview(book.id, "이 책 정말 좋았어요!", 5, accessTokens.get(0))
        val review =
            reviewService.findLatest().orElseThrow(Supplier { RuntimeException("리뷰가 없습니다.") })

        val resultActions = updateRecommendReview(review.id, true, accessTokens.get(0))
        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewRecommendController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("modifyRecommendReview"))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("404-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Review recommendation not found"))
    }

    @Test
    @DisplayName("리뷰 추천 취소 - 성공")
    @Throws(Exception::class)
    fun t9() {
        val accessTokens = makeAccessTokens(1)
        val book = bookRepository.findAll().get(0)
        createReview(book.id, "이 책 정말 좋았어요!", 5, accessTokens.get(0))
        val review =
            reviewService.findLatest().orElseThrow(Supplier { RuntimeException("리뷰가 없습니다.") })

        createRecommendReview(review.id, true, accessTokens.get(0)).andDo(MockMvcResultHandlers.print())

        val resultActions = deleteRecommendReview(review.id, accessTokens.get(0))
        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewRecommendController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("cancelRecommendReview"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Review recommendation cancelled successfully"))
    }

    @Test
    @DisplayName("리뷰 추천 취소 - 실패 (추천하지 않은 리뷰)")
    @Throws(Exception::class)
    fun t10() {
        val accessTokens = makeAccessTokens(1)
        val book = bookRepository.findAll().get(0)
        createReview(book.id, "이 책 정말 좋았어요!", 5, accessTokens.get(0))
        val review =
            reviewService.findLatest().orElseThrow(Supplier { RuntimeException("리뷰가 없습니다.") })

        val resultActions = deleteRecommendReview(review.id, accessTokens.get(0))
        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewRecommendController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("cancelRecommendReview"))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("404-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Review recommendation not found"))
    }

    @Test
    @DisplayName("리뷰 추천 취소 - 실패 (없는 리뷰)")
    @Throws(Exception::class)
    fun t11() {
        val accessTokens = makeAccessTokens(1)
        val book = bookRepository.findAll().get(0)
        createReview(book.id, "이 책 정말 좋았어요!", 5, accessTokens.get(0))
        val review =
            reviewService.findLatest().orElseThrow(Supplier { RuntimeException("리뷰가 없습니다.") })

        val resultActions = deleteRecommendReview(-1, accessTokens.get(0))
        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewRecommendController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("cancelRecommendReview"))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("404-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Review not found"))
    }

    @Test
    @DisplayName("리뷰 추천 취소 - 취소한 리뷰 취소")
    @Throws(Exception::class)
    fun t12() {
        val accessTokens = makeAccessTokens(1)
        val book = bookRepository.findAll().get(0)
        createReview(book.id, "이 책 정말 좋았어요!", 5, accessTokens.get(0))
        val review =
            reviewService.findLatest().orElseThrow(Supplier { RuntimeException("리뷰가 없습니다.") })

        createRecommendReview(review.id, true, accessTokens.get(0)).andDo(MockMvcResultHandlers.print())
        deleteRecommendReview(review.id, accessTokens.get(0)).andDo(MockMvcResultHandlers.print())

        val resultActions = deleteRecommendReview(review.id, accessTokens.get(0))
        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewRecommendController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("cancelRecommendReview"))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("404-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Review recommendation not found"))
    }


    @Test
    @DisplayName("리뷰 동시성 체크 생성 - 성공")
    @Throws(Exception::class)
    fun t13() {
        val memberCount = 100
        val accessTokens = makeAccessTokens(memberCount)
        val book = bookRepository.findAll().get(0)
        createReview(book.id, "이 책 정말 좋았어요!", 5, accessTokens.get(0))
        var review =
            reviewService.findLatest().orElseThrow(Supplier { RuntimeException("리뷰가 없습니다.") })
        val threadCount = 20
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(memberCount)
        for (i in 0..<memberCount) {
            val finalReview = review
            val accessToken = accessTokens.get(i)
            executor.submit(Runnable {
                try {
                    val result = createRecommendReview(finalReview.id, true, accessToken)
                    result
                        .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewRecommendController::class.java))
                        .andExpect(MockMvcResultMatchers.handler().methodName("recommendReview"))
                        .andExpect(MockMvcResultMatchers.status().isCreated())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("201-1"))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Review recommended successfully"))
                } catch (e: Exception) {
                    throw RuntimeException(e)
                } finally {
                    latch.countDown()
                }
            })
        }
        latch.await()
        em.clear()
        val count = reviewRecommendRepository.count()
        AssertionsForClassTypes.assertThat(count).isEqualTo(memberCount.toLong())
        review = reviewRepository.findById(review.id).get()
        AssertionsForClassTypes.assertThat(review.likeCount).isEqualTo(memberCount)
    }

    @Test
    @DisplayName("리뷰 동시성 체크 수정 - 성공")
    @Throws(Exception::class)
    fun t14() {
        val memberCount = 100
        val accessTokens = makeAccessTokens(memberCount)
        val book = bookRepository.findAll().get(0)
        createReview(book.id, "이 책 정말 좋았어요!", 5, accessTokens.get(0))
        var review =
            reviewService.findLatest().orElseThrow(Supplier { RuntimeException("리뷰가 없습니다.") })
        val threadCount = 20
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(memberCount)
        for (i in 0..<memberCount) {
            val finalReview = review
            val accessToken = accessTokens.get(i)
            executor.submit(Runnable {
                try {
                    val result = createRecommendReview(finalReview.id, true, accessToken)
                    result
                        .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewRecommendController::class.java))
                        .andExpect(MockMvcResultMatchers.handler().methodName("recommendReview"))
                        .andExpect(MockMvcResultMatchers.status().isCreated())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("201-1"))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Review recommended successfully"))
                } catch (e: Exception) {
                    throw RuntimeException(e)
                } finally {
                    latch.countDown()
                }
            })
        }
        latch.await()
        em!!.clear()
        val executor2 = Executors.newFixedThreadPool(threadCount)
        val latch2 = CountDownLatch(memberCount)
        for (i in 0..<memberCount) {
            val finalReview = review
            val accessToken = accessTokens.get(i)
            executor2.submit(Runnable {
                try {
                    val result2 = updateRecommendReview(finalReview.id, false, accessToken)
                    result2
                        .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewRecommendController::class.java))
                        .andExpect(MockMvcResultMatchers.handler().methodName("modifyRecommendReview"))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
                } catch (e: Exception) {
                    throw RuntimeException(e)
                } finally {
                    latch2.countDown()
                }
            })
        }
        latch2.await()
        em.clear()
        val count = reviewRecommendRepository!!.count()
        AssertionsForClassTypes.assertThat(count).isEqualTo(memberCount.toLong())
        review = reviewRepository.findById(review.id).get()
        AssertionsForClassTypes.assertThat(review.dislikeCount).isEqualTo(memberCount)
    }

    @Test
    @DisplayName("리뷰 동시성 체크 삭제 - 성공")
    @Throws(Exception::class)
    fun t15() {
        val memberCount = 100
        val accessTokens = makeAccessTokens(memberCount)
        val book = bookRepository.findAll().get(0)
        createReview(book.id, "이 책 정말 좋았어요!", 5, accessTokens.get(0))
        var review =
            reviewService.findLatest().orElseThrow(Supplier { RuntimeException("리뷰가 없습니다.") })
        val threadCount = 20
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(memberCount)
        for (i in 0..<memberCount) {
            val finalReview = review
            val accessToken = accessTokens.get(i)
            executor.submit(Runnable {
                try {
                    val result = createRecommendReview(finalReview.id, true, accessToken)
                    result
                        .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewRecommendController::class.java))
                        .andExpect(MockMvcResultMatchers.handler().methodName("recommendReview"))
                        .andExpect(MockMvcResultMatchers.status().isCreated())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("201-1"))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Review recommended successfully"))
                } catch (e: Exception) {
                    throw RuntimeException(e)
                } finally {
                    latch.countDown()
                }
            })
        }
        latch.await()
        em.clear()
        val executor2 = Executors.newFixedThreadPool(threadCount)
        val latch2 = CountDownLatch(memberCount)
        for (i in 0..<memberCount) {
            val finalReview = review
            val accessToken = accessTokens.get(i)
            executor2.submit( {
                try {
                    val result2 = deleteRecommendReview(finalReview.id, accessToken)
                    result2
                        .andExpect(MockMvcResultMatchers.handler().handlerType(ReviewRecommendController::class.java))
                        .andExpect(MockMvcResultMatchers.handler().methodName("modifyRecommendReview"))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
                } catch (e: Exception) {
                    throw RuntimeException(e)
                } finally {
                    latch2.countDown()
                }
            })
        }
        latch2.await()
        em.clear()
        val count = reviewRecommendRepository.count()
        AssertionsForClassTypes.assertThat(count).isEqualTo(0)
        review = reviewRepository.findById(review.id).get()
        AssertionsForClassTypes.assertThat(review.likeCount).isEqualTo(0)
    }
}
