package com.back.domain.review.reviewRecommend.controller;

import com.back.domain.book.book.entity.Book;
import jakarta.persistence.EntityManager;
import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.review.review.controller.ReviewController;
import com.back.domain.review.review.entity.Review;
import com.back.domain.review.review.repository.ReviewRepository;
import com.back.domain.review.review.service.ReviewService;
import com.back.domain.review.reviewRecommend.repository.ReviewRecommendRepository;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;


import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@Rollback
public class ReviewRecommendControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReviewRecommendRepository reviewRecommendRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    List<String> makeAccessTokens(int count){
        return memberRepository.findAll().stream()
                .limit(count)
                .map(member -> memberService.geneAccessToken(member))
                .toList();
    }

    void createReview(int bookId, String content, int rate, String accessToken) throws Exception{
        mvc.perform(
                post("/reviews/{book_id}", bookId)
                        .contentType("application/json")
                        .content("""
    {
        "content": "%s",
        "rate": "%d"
        }
""".formatted(content, rate))
                        .cookie(new Cookie("accessToken", accessToken))
        ).andDo(print());
    }

    ResultActions createRecommendReview(int reviewId, boolean isRecommend, String accessToken) throws Exception{
        return mvc.perform(
                post("/reviewRecommend/{review_id}/{isRecommend}", reviewId, isRecommend)
                        .cookie(new Cookie("accessToken", accessToken))
        ).andDo(print());
    }

    ResultActions updateRecommendReview(int reviewId, boolean isRecommend, String accessToken) throws Exception{
        return mvc.perform(
                put("/reviewRecommend/{review_id}/{isRecommend}", reviewId, isRecommend)
                        .cookie(new Cookie("accessToken", accessToken))
        ).andDo(print());
    }

    ResultActions deleteRecommendReview(int reviewId, String accessToken) throws Exception{
        return mvc.perform(
                delete("/reviewRecommend/{review_id}", reviewId)
                        .cookie(new Cookie("accessToken", accessToken))
        ).andDo(print());
    }

    @Test
    @DisplayName("리뷰 추천하기 - 성공")
    void t1() throws Exception{
        Member member = memberService.findByEmail("email1@a.a").get();
        String accessToken = memberService.geneAccessToken(member);
        Book book = bookRepository.findAll().get(0);
        createReview(book.getId(), "이 책 정말 좋았어요!", 5, accessToken);
        Review review = reviewService.findLatest().orElseThrow(()-> new RuntimeException("리뷰가 없습니다."));


        ResultActions resultActions = mvc.perform(
                post("/reviewRecommend/{review_id}/{isRecommend}", review.getId(), true)
                        .cookie(new Cookie("accessToken", accessToken))
        ).andDo(print());
        resultActions
                .andExpect(handler().handlerType(ReviewRecommendController.class))
                .andExpect(handler().methodName("recommendReview"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("Review recommended successfully"))
        ;

    }

    @Test
    @DisplayName("리뷰 여러명이 추천하기 - 성공")
    void t2() throws Exception{
        Member member = memberService.findByEmail("email1@a.a").get();
        Member member2 = memberService.findByEmail("email2@a.a").get();
        Member member3 = memberService.findByEmail("email3@a.a").get();
        Member member4 = memberService.findByEmail("email4@a.a").get();
        String accessToken = memberService.geneAccessToken(member);
        String accessToken2 = memberService.geneAccessToken(member2);
        String accessToken3 = memberService.geneAccessToken(member3);
        String accessToken4 = memberService.geneAccessToken(member4);
        Book book = bookRepository.findAll().get(0);
        createReview(book.getId(), "이 책 정말 좋았어요!", 5, accessToken);


        Review review = reviewService.findLatest().orElseThrow(()-> new RuntimeException("리뷰가 없습니다."));
        mvc.perform(
                post("/reviewRecommend/{review_id}/{isRecommend}", review.getId(), true)
                        .cookie(new Cookie("accessToken", accessToken))
        ).andDo(print());

        ResultActions resultActions = mvc.perform(
                post("/reviewRecommend/{review_id}/{isRecommend}", review.getId(), false)
                        .cookie(new Cookie("accessToken", accessToken2))
        ).andDo(print());

        mvc.perform(
                post("/reviewRecommend/{review_id}/{isRecommend}", review.getId(), true)
                        .cookie(new Cookie("accessToken", accessToken3))
        ).andDo(print());

        mvc.perform(
                post("/reviewRecommend/{review_id}/{isRecommend}", review.getId(), false)
                        .cookie(new Cookie("accessToken", accessToken4))
        ).andDo(print());
        resultActions
                .andExpect(handler().handlerType(ReviewRecommendController.class))
                .andExpect(handler().methodName("recommendReview"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("Review recommended successfully"))
        ;
        assertThat(review.getLikeCount()).isEqualTo(2);
        assertThat(review.getDislikeCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("리뷰 추천하기 - 실패 (이미 추천한 리뷰)")
    void t3() throws Exception{
        Member member = memberService.findByEmail("email1@a.a").get();
        String accessToken = memberService.geneAccessToken(member);
        Book book = bookRepository.findAll().get(0);
        createReview(book.getId(), "이 책 정말 좋았어요!", 5, accessToken);

        Review review = reviewService.findLatest().orElseThrow(()-> new RuntimeException("리뷰가 없습니다."));
        mvc.perform(
                post("/reviewRecommend/{review_id}/{isRecommend}", review.getId(), true)
                        .cookie(new Cookie("accessToken", accessToken))
        ).andDo(print());
        ResultActions resultActions = mvc.perform(
                post("/reviewRecommend/{review_id}/{isRecommend}", review.getId(), true)
                        .cookie(new Cookie("accessToken", accessToken))
        ).andDo(print());
        resultActions
                .andExpect(handler().handlerType(ReviewRecommendController.class))
                .andExpect(handler().methodName("recommendReview"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("Review recommendation already exists"))
        ;
    }

    @Test
    @DisplayName("없는 리뷰 추천하기 - 실패")
    void t4()throws Exception{
        Member member = memberService.findByEmail("email1@a.a").get();
        String accessToken = memberService.geneAccessToken(member);
        Book book = bookRepository.findAll().get(0);
        createReview(book.getId(), "이 책 정말 좋았어요!", 5, accessToken);
        Review review = reviewService.findLatest().orElseThrow(()-> new RuntimeException("리뷰가 없습니다."));
        ResultActions resultActions = createRecommendReview(-1, true, accessToken);
        resultActions
                .andExpect(handler().handlerType(ReviewRecommendController.class))
                .andExpect(handler().methodName("recommendReview"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("Review not found"))
                ;
    }

    @Test
    @DisplayName("리뷰 추천 업데이트 - 성공")
    void t5() throws Exception{
        List<String> accessTokens = makeAccessTokens(10);
        Book book = bookRepository.findAll().get(0);
        createReview(book.getId(), "이 책 정말 좋았어요!", 5, accessTokens.get(0));


        Review review = reviewService.findLatest().orElseThrow(()-> new RuntimeException("리뷰가 없습니다."));

        for (int i = 0; i < accessTokens.size(); i++) {
            boolean isRecommend = i % 2 == 0; // 짝수 인덱스는 추천, 홀수 인덱스는 비추천
            createRecommendReview(review.getId(), isRecommend, accessTokens.get(i)).andDo(print());
        }

        for (int i = 0; i < accessTokens.size(); i++) {
            boolean isRecommend = i % 2 != 0; // 짝수 인덱스는 비추천, 홀수 인덱스는 추천
            updateRecommendReview(review.getId(), isRecommend, accessTokens.get(i)).andDo(print());
        }

        assertThat(review.getLikeCount()).isEqualTo(accessTokens.size() / 2);
        assertThat(review.getDislikeCount()).isEqualTo(accessTokens.size() / 2);
    }

    @Test
    @DisplayName("리뷰 추천 업데이트 - 실패")
    void t6() throws Exception{
        List<String> accessTokens = makeAccessTokens(10);
        Book book = bookRepository.findAll().get(0);
        createReview(book.getId(), "이 책 정말 좋았어요!", 5, accessTokens.get(0));
        Review review = reviewService.findLatest().orElseThrow(()-> new RuntimeException("리뷰가 없습니다."));

        for (int i = 0; i < accessTokens.size(); i++) {
            boolean isRecommend = i % 2 == 0; // 짝수 인덱스는 추천, 홀수 인덱스는 비추천
            createRecommendReview(review.getId(), isRecommend, accessTokens.get(i)).andDo(print());
        }

        ResultActions resultActions = updateRecommendReview(-1, true, accessTokens.get(0));
        resultActions
                .andExpect(handler().handlerType(ReviewRecommendController.class))
                .andExpect(handler().methodName("modifyRecommendReview"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("Review not found"))
                ;
    }

    @Test
    @DisplayName("리뷰 추천 업데이트 - 실패 (이미 추천한 리뷰)")
    void t7() throws Exception{
        List<String> accessTokens = makeAccessTokens(1);
        Book book = bookRepository.findAll().get(0);
        createReview(book.getId(), "이 책 정말 좋았어요!", 5, accessTokens.get(0));
        Review review = reviewService.findLatest().orElseThrow(()-> new RuntimeException("리뷰가 없습니다."));

        createRecommendReview(review.getId(), true, accessTokens.get(0)).andDo(print());

        ResultActions resultActions = updateRecommendReview(review.getId(), true, accessTokens.get(0));
        resultActions
                .andExpect(handler().handlerType(ReviewRecommendController.class))
                .andExpect(handler().methodName("modifyRecommendReview"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-2"))
                .andExpect(jsonPath("$.msg").value("Review recommendation already set to this value"))
        ;
    }

    @Test
    @DisplayName("리뷰 추천 업데이트 - 실패 (추천하지 않은 리뷰)")
    void t8() throws Exception{
        List<String> accessTokens = makeAccessTokens(1);
        Book book = bookRepository.findAll().get(0);
        createReview(book.getId(), "이 책 정말 좋았어요!", 5, accessTokens.get(0));
        Review review = reviewService.findLatest().orElseThrow(()-> new RuntimeException("리뷰가 없습니다."));

        ResultActions resultActions = updateRecommendReview(review.getId(), true, accessTokens.get(0));
        resultActions
                .andExpect(handler().handlerType(ReviewRecommendController.class))
                .andExpect(handler().methodName("modifyRecommendReview"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("Review recommendation not found"))
        ;
    }

    @Test
    @DisplayName("리뷰 추천 취소 - 성공")
    void t9() throws Exception{
        List<String> accessTokens = makeAccessTokens(1);
        Book book = bookRepository.findAll().get(0);
        createReview(book.getId(), "이 책 정말 좋았어요!", 5, accessTokens.get(0));
        Review review = reviewService.findLatest().orElseThrow(()-> new RuntimeException("리뷰가 없습니다."));

        createRecommendReview(review.getId(), true, accessTokens.get(0)).andDo(print());

        ResultActions resultActions = deleteRecommendReview(review.getId(), accessTokens.get(0));
        resultActions
                .andExpect(handler().handlerType(ReviewRecommendController.class))
                .andExpect(handler().methodName("cancelRecommendReview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("Review recommendation cancelled successfully"))
        ;
    }

    @Test
    @DisplayName("리뷰 추천 취소 - 실패 (추천하지 않은 리뷰)")
    void t10() throws Exception{
        List<String> accessTokens = makeAccessTokens(1);
        Book book = bookRepository.findAll().get(0);
        createReview(book.getId(), "이 책 정말 좋았어요!", 5, accessTokens.get(0));
        Review review = reviewService.findLatest().orElseThrow(()-> new RuntimeException("리뷰가 없습니다."));

        ResultActions resultActions = deleteRecommendReview(review.getId(), accessTokens.get(0));
        resultActions
                .andExpect(handler().handlerType(ReviewRecommendController.class))
                .andExpect(handler().methodName("cancelRecommendReview"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("Review recommendation not found"))
        ;
    }

    @Test
    @DisplayName("리뷰 추천 취소 - 실패 (없는 리뷰)")
    void t11() throws Exception{
        List<String> accessTokens = makeAccessTokens(1);
        Book book = bookRepository.findAll().get(0);
        createReview(book.getId(), "이 책 정말 좋았어요!", 5, accessTokens.get(0));
        Review review = reviewService.findLatest().orElseThrow(()-> new RuntimeException("리뷰가 없습니다."));

        ResultActions resultActions = deleteRecommendReview(-1, accessTokens.get(0));
        resultActions
                .andExpect(handler().handlerType(ReviewRecommendController.class))
                .andExpect(handler().methodName("cancelRecommendReview"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("Review not found"))
        ;
    }

    @Test
    @DisplayName("리뷰 추천 취소 - 취소한 리뷰 취소")
    void t12() throws Exception{
        List<String> accessTokens = makeAccessTokens(1);
        Book book = bookRepository.findAll().get(0);
        createReview(book.getId(), "이 책 정말 좋았어요!", 5, accessTokens.get(0));
        Review review = reviewService.findLatest().orElseThrow(()-> new RuntimeException("리뷰가 없습니다."));

        createRecommendReview(review.getId(), true, accessTokens.get(0)).andDo(print());
        deleteRecommendReview(review.getId(), accessTokens.get(0)).andDo(print());

        ResultActions resultActions = deleteRecommendReview(review.getId(), accessTokens.get(0));
        resultActions
                .andExpect(handler().handlerType(ReviewRecommendController.class))
                .andExpect(handler().methodName("cancelRecommendReview"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("Review recommendation not found"))
        ;
    }

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("리뷰 동시성 체크 생성 - 성공")
    void t13() throws Exception{
        int memberCount = 100;
        List<String> accessTokens = makeAccessTokens(memberCount);
        Book book = bookRepository.findAll().get(0);
        createReview(book.getId(), "이 책 정말 좋았어요!", 5, accessTokens.get(0));
        Review review = reviewService.findLatest().orElseThrow(()-> new RuntimeException("리뷰가 없습니다."));
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(memberCount);
        for (int i = 0; i< memberCount; i++){
            Review finalReview = review;
            String accessToken = accessTokens.get(i);
            executor.submit(() -> {
                try{
                    ResultActions result = createRecommendReview(finalReview.getId(), true, accessToken);
                    result
                            .andExpect(handler().handlerType(ReviewRecommendController.class))
                            .andExpect(handler().methodName("recommendReview"))
                            .andExpect(status().isCreated())
                            .andExpect(jsonPath("$.resultCode").value("201-1"))
                            .andExpect(jsonPath("$.msg").value("Review recommended successfully"))
                    ;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        em.clear();
        long count = reviewRecommendRepository.count();
        assertThat(count).isEqualTo(memberCount);
        review = reviewRepository.findById(review.getId()).get();
        assertThat(review.getLikeCount()).isEqualTo(memberCount);
    }

    @Test
    @DisplayName("리뷰 동시성 체크 수정 - 성공")
    void t14() throws Exception{
        int memberCount = 100;
        List<String> accessTokens = makeAccessTokens(memberCount);
        Book book = bookRepository.findAll().get(0);
        createReview(book.getId(), "이 책 정말 좋았어요!", 5, accessTokens.get(0));
        Review review = reviewService.findLatest().orElseThrow(()-> new RuntimeException("리뷰가 없습니다."));
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(memberCount);
        for (int i = 0; i< memberCount; i++){
            Review finalReview = review;
            String accessToken = accessTokens.get(i);
            executor.submit(() -> {
                try{
                    ResultActions result = createRecommendReview(finalReview.getId(), true, accessToken);
                    result
                            .andExpect(handler().handlerType(ReviewRecommendController.class))
                            .andExpect(handler().methodName("recommendReview"))
                            .andExpect(status().isCreated())
                            .andExpect(jsonPath("$.resultCode").value("201-1"))
                            .andExpect(jsonPath("$.msg").value("Review recommended successfully"))
                    ;

                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        em.clear();
        ExecutorService executor2 = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch2 = new CountDownLatch(memberCount);
        for (int i = 0; i< memberCount; i++){
            Review finalReview = review;
            String accessToken = accessTokens.get(i);
            executor2.submit(() -> {
                try{
                    ResultActions result2 = updateRecommendReview(finalReview.getId(), false, accessToken);
                    result2
                            .andExpect(handler().handlerType(ReviewRecommendController.class))
                            .andExpect(handler().methodName("modifyRecommendReview"))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.resultCode").value("200-1"))
                    ;
                }catch (Exception e) {
                    throw new RuntimeException(e);
                }finally {
                    latch2.countDown();
                }
            });
        }
        latch2.await();
        em.clear();
        long count = reviewRecommendRepository.count();
        assertThat(count).isEqualTo(memberCount);
        review = reviewRepository.findById(review.getId()).get();
        assertThat(review.getDislikeCount()).isEqualTo(memberCount);
    }

    @Test
    @DisplayName("리뷰 동시성 체크 삭제 - 성공")
    void t15() throws Exception{
        int memberCount = 100;
        List<String> accessTokens = makeAccessTokens(memberCount);
        Book book = bookRepository.findAll().get(0);
        createReview(book.getId(), "이 책 정말 좋았어요!", 5, accessTokens.get(0));
        Review review = reviewService.findLatest().orElseThrow(()-> new RuntimeException("리뷰가 없습니다."));
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(memberCount);
        for (int i = 0; i< memberCount; i++){
            Review finalReview = review;
            String accessToken = accessTokens.get(i);
            executor.submit(() -> {
                try{
                    ResultActions result = createRecommendReview(finalReview.getId(), true, accessToken);
                    result
                            .andExpect(handler().handlerType(ReviewRecommendController.class))
                            .andExpect(handler().methodName("recommendReview"))
                            .andExpect(status().isCreated())
                            .andExpect(jsonPath("$.resultCode").value("201-1"))
                            .andExpect(jsonPath("$.msg").value("Review recommended successfully"))
                    ;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        em.clear();
        ExecutorService executor2 = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch2 = new CountDownLatch(memberCount);
        for(int i = 0; i< memberCount; i++){
            Review finalReview = review;
            String accessToken = accessTokens.get(i);
            executor2.submit(() -> {
                try{
                    ResultActions result2 = deleteRecommendReview(finalReview.getId(), accessToken);
                    result2
                            .andExpect(handler().handlerType(ReviewRecommendController.class))
                            .andExpect(handler().methodName("modifyRecommendReview"))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.resultCode").value("200-1"))
                    ;
                }catch (Exception e) {
                    throw new RuntimeException(e);
                }finally {
                    latch2.countDown();
                }
            });
        }
        latch2.await();
        em.clear();
        long count = reviewRecommendRepository.count();
        assertThat(count).isEqualTo(0);
        review = reviewRepository.findById(review.getId()).get();
        assertThat(review.getLikeCount()).isEqualTo(0);
    }
}
